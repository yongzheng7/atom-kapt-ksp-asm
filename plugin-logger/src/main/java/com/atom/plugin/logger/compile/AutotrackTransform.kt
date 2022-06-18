package com.atom.plugin.logger.compile


import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.atom.plugin.core.Log
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.net.URLClassLoader
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class AutotrackTransform : Transform() {

    private lateinit var mLog: Log
    private var mAndroidJars: kotlin.collections.List<URL>? = null

    private var mOutputProvider: TransformOutputProvider? = null
    private var mDirectoryInput: DirectoryInput? = null
    private var mClassRewriter: ClassRewriter? = null
    private var mExecutor: BuildExecutor? = null
    private var mAutotrackExtension: AutotrackExtension? = null

    fun AutotrackTransform(project: Project) {
        mAutotrackExtension = project.extensions.getByType(AutotrackExtension::class.java)
    }

    fun setAndroidJars(androidJars: kotlin.collections.List<URL>?) {
        mAndroidJars = androidJars
    }

    override fun getName(): String? {
        return "growingAutotracker"
    }

    override fun getInputTypes(): Set<QualifiedContent.ContentType?>? {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return true
    }

    private fun log(msg: String) {
        mLog.i(msg)
    }

    @kotlin.jvm.Throws(IOException::class, InterruptedException::class)
    override fun transform(
        context: Context?,
        inputs: kotlin.collections.Collection<TransformInput>,
        referencedInputs: kotlin.collections.Collection<TransformInput?>?,
        outputProvider: TransformOutputProvider,
        isIncremental: Boolean
    ) {
        mLog.i("transform task start: isIncremental = $isIncremental")
        mExecutor = BuildExecutor.createExecutor()
        val urlList = ArrayList<URL>()
        for (input in inputs) {
            for (directoryInput in input.directoryInputs) {
                urlList.add(directoryInput.file.toURL())
            }
            for (jarInput in input.jarInputs) {
                urlList.add(jarInput.file.toURL())
            }
        }
        urlList.addAll(mAndroidJars!!)
        val urlArray = arrayOfNulls<URL>(urlList.size)
        urlList.toArray(urlArray)
        val classLoader = URLClassLoader(urlArray)
        mOutputProvider = outputProvider
        mClassRewriter = ClassRewriter(
            mLog,
            classLoader,
            mAutotrackExtension!!.getExcludePackages(),
            mAutotrackExtension!!.isExcludeOfficialPackages()
        )
        if (!isIncremental) {
            // 1. 非增量模式下删除上次所有的编译产物
            try {
                outputProvider.deleteAll()
            } catch (e: IOException) {
                mLog.e("删除上次的编译产物失败: " + e.message)
                throw e
            }
        }
        for (transformInput in inputs) {
            for (directoryInput in transformInput.directoryInputs) {
                mDirectoryInput = directoryInput
                if (isIncremental) {
                    // 2. 增量模式下处理directory
                    transformInputDirectoryIncrement()
                } else {
                    // 3. 非增量模式处理directory
                    transformInputDirectoryNoIncrement()
                }
            }
            mDirectoryInput = null
            for (jarInput in transformInput.jarInputs) {
                mExecutor?.execute(Runnable { // 4. 处理jar包, 整个jar包暂时在同一个线程中处理, jar包更改的概率很小, 不会影响instant run
                    transformJar(jarInput, isIncremental)
                })
            }
        }
        mLog.w("has submit all gio task, and wait for all task complete")
        mExecutor?.waitAllTaskComplete()
        mLog.w("transform task completed")

        // reset tmp variable
        if (classLoader != null) {
            classLoader.close()
        }
        mOutputProvider = null
        mDirectoryInput = null
        mClassRewriter = null
        mExecutor = null
    }

    private fun transformJar(jarInput: JarInput, isIncremental: Boolean) {
        val out = mOutputProvider!!.getContentLocation(
            jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR
        )
        out.parentFile.mkdirs()
        if (isIncremental && jarInput.status == Status.NOTCHANGED) {
            return
        }
        if (out.exists()) {
            FileUtils.deleteQuietly(out)
        }
        if (isIncremental && jarInput.status == Status.REMOVED) {
            return
        }
        log("transforming " + jarInput.file + " to jar: " + out)
        try {
            ZipOutputStream(FileOutputStream(out)).use { outJar ->
                ZipInputStream(FileInputStream(jarInput.file)).use { jar ->
                    var entry: ZipEntry
                    while (jar.nextEntry.also { entry = it } != null) {
                        val outEntry = copyEntry(entry)
                        outJar.putNextEntry(outEntry)
                        if (!entry.isDirectory
                            && entry.name != "module-info.class"
                            && entry.name.endsWith(".class")
                        ) {
                            if (mClassRewriter!!.transformClass(jar, outJar)) {
                                log("transforming jar entry: " + entry.name)
                            }
                        } else {
                            IOUtils.copy(jar, outJar)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun transformInputDirectoryIncrement() {
        val changedFiles = mDirectoryInput!!.changedFiles
        for (file in changedFiles.keys) {
            val status = changedFiles[file]
            actionOnFile(file, status)
        }
    }

    // 5. 处理directoryInput中的文件, 可以单个在子线程中处理
    private fun actionOnFile(file: File, status: Status?) {
        val outDir = mOutputProvider!!.getContentLocation(
            mDirectoryInput!!.name,
            mDirectoryInput!!.contentTypes, mDirectoryInput!!.scopes, Format.DIRECTORY
        )
        outDir.mkdirs()
        val outDirPath = outDir.absolutePath
        val inputDirPath = mDirectoryInput!!.file.absolutePath
        val relativeClassPath = file.absolutePath.substring(inputDirPath.length)
        mExecutor?.execute {
            val outFile = File(outDirPath, relativeClassPath)
            if (status == Status.REMOVED) {
                FileUtils.deleteQuietly(outFile)
            } else if (status != Status.NOTCHANGED) {
                if (relativeClassPath.endsWith(".class")) {
                    if (mClassRewriter!!.transformClassFile(file, outFile)) {
                        log("transformed class file $file to $outFile")
                        return@execute
                    }
                }
                try {
                    FileUtils.copyFile(file, outFile)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun transformInputDirectoryNoIncrement() {
        for (classFile in com.android.utils.FileUtils.getAllFiles(mDirectoryInput!!.file)) {
            actionOnFile(classFile, Status.ADDED)
        }
    }

    private fun copyEntry(entry: ZipEntry): ZipEntry {
        val newEntry = ZipEntry(entry.name)
        newEntry.comment = entry.comment
        newEntry.extra = entry.extra
        return newEntry
    }

}