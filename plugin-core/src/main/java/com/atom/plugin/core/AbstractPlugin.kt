package com.atom.plugin.core

import com.android.annotations.NonNull
import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.collect.FluentIterable
import com.google.common.collect.Sets
import com.google.common.io.Files
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginContainer
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AbstractPlugin
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/3/15
 * @Copyright: 2022/3/15 www.rongdasoft.com Inc. All rights reserved.
 */
abstract class AbstractPlugin<E : AbstractExtension> : Transform(), Plugin<Project> {

    protected lateinit var project: Project
    protected var extension: E? = null

    private var isApp: Boolean = true
    private lateinit var executor: ExecutorService
    private val tasks = mutableListOf<Callable<Unit>>()

    abstract fun getExtensionName(): String

    abstract fun getExtensionClass(): Class<E>

    override fun apply(project: Project) {
        Log.init(project)
        this.project = project
        val plugins: PluginContainer = project.plugins
        val hasAppPlugin: Boolean = plugins.hasPlugin(AppPlugin::class.java)
        hasAppPlugin.isTrue {
            val extensions: ExtensionContainer = project.extensions
            this.isApp = extensions is AppExtension
            this.extension = extensions.create(getExtensionName(), getExtensionClass())
        }
    }

    override fun getName(): String = getExtensionName()

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return if (isApp) {
            TransformManager.SCOPE_FULL_PROJECT
        } else {
            Sets.immutableEnumSet(QualifiedContent.Scope.PROJECT)
        }
    }

    override fun isIncremental(): Boolean = true

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        if (transformInvocation == null) {
            return
        }
        this.extension?.also { e ->
            this.executor = if (e.runSingle) {
                Executors.newSingleThreadExecutor()
            } else {
                ForkJoinPool.commonPool()
            }
            try {
                beforeTransform(transformInvocation, e)
                runTransform(transformInvocation)
                afterTransform(transformInvocation, e)
            } catch (e: Throwable) {
                Log.e("transform error" + e.localizedMessage)
                e.printStackTrace()
            }
        }
    }

    protected fun beforeTransform(transformInvocation: TransformInvocation, e: E) {
        Log.e("-------------------------beforeTransform------------------------")
        Log.e("ProjectName  : ${project.name}")
        Log.e("ExtensionName: ${getExtensionName()}")
        Log.e("ProjectPath  : ${transformInvocation.context.path}")
        Log.e("BuildType    : ${transformInvocation.context.variantName}")
        Log.e("Incremental  : ${transformInvocation.isIncremental}")
        Log.e("extension    : $e")
        Log.e("Time         : ${Date()}")
        Log.e("----------------------------------------------------------------")
    }

    private fun runTransform(transformInvocation: TransformInvocation) {
        val isIncremental = transformInvocation.isIncremental
        //如果非增量，则清空旧的输出内容
        if (!isIncremental) {
            transformInvocation.outputProvider.deleteAll()
        }
        Log.e("|----transformInvocation.inputs [${transformInvocation.inputs.size}]--------------------------------")
        transformInvocation.inputs.forEach { transformInput ->
            Log.e("----jarInputs[${transformInput.jarInputs.size}]----------------------------")
            transformInput.jarInputs.forEach { jarInput ->
                eachJar(
                    transformInvocation,
                    isIncremental,
                    jarInput
                )
            }
            Log.e("----directoryInputs[${transformInput.directoryInputs.size}]----------------------------")
            transformInput.directoryInputs.forEach { directoryInput ->
                eachDir(
                    transformInvocation,
                    isIncremental,
                    directoryInput
                )
            }
        }
        executor.invokeAll(tasks)
    }

    protected fun afterTransform(transformInvocation: TransformInvocation, e: E) {
        Log.e("-----------------------afterTransform---------------------------")
        Log.e("ProjectName  : ${project.name}")
        Log.e("Tasks        : ${tasks.size}")
        Log.e("extension    : $e")
        Log.e("Time         : ${Date()}")
        Log.e("----------------------------------------------------------------")
    }

    private fun eachDir(
        transformInvocation: TransformInvocation,
        isIncremental: Boolean,
        directoryInput: DirectoryInput
    ) {
        val dest = transformInvocation.outputProvider.getContentLocation(
            directoryInput.name,
            directoryInput.contentTypes,
            directoryInput.scopes,
            Format.DIRECTORY
        )
        FileUtils.forceMkdir(dest)
        val biConsumer =
            BiConsumer { file: File, status: Status ->
                try {
                    // D:\app_git_android\demo_asm\test-kapt-ksp\app\build\intermediates\javac\debug\classes
                    val srcDirPath = directoryInput.file.absolutePath
                    // D:\app_git_android\demo_asm\test-kapt-ksp\app\build\intermediates\transforms\logPlugin\debug\47
                    val destDirPath = dest.absolutePath
                    // D:\app_git_android\demo_asm\test-kapt-ksp\app\build\intermediates\javac\debug\classes\com\atom\test\annotation\BuildConfig.class
                    val srcFilePath = file.absolutePath
                    // D:\app_git_android\demo_asm\test-kapt-ksp\app\build\intermediates\transforms\logPlugin\debug\47\com\atom\test\annotation\BuildConfig.class
                    val destFilePath =
                        srcFilePath.replace(srcDirPath, destDirPath)
                    val destFile = File(destFilePath)
                    when (status) {
                        Status.NOTCHANGED -> {}
                        Status.REMOVED -> if (destFile.exists()) {
                            FileUtils.forceDelete(destFile)
                        }
                        Status.ADDED, Status.CHANGED -> {
                            try {
                                FileUtils.touch(destFile)
                            } catch (e: Throwable) {
                                val pr = destFile.parentFile
                                if (!pr.exists()) {
                                    pr.mkdirs()
                                }
                            }
                            tasks.add(Callable<Unit> { mainSingleClassToFile(file, destFile) })
                            Log.e("foreach Dir > status=${status} \n inputDirPath=${srcDirPath}  outputDirPath=${destDirPath} \n inputFilePath=${file.absolutePath} outputFilePath=${destFilePath}")
                        }
                    }
                } catch (e: IOException) {
                    Log.e("foreach Dir error 1 ${e}")
                }
            }
        //当前是否是增量编译
        if (isIncremental) {
            directoryInput.changedFiles.forEach(biConsumer)
        } else {
            getAllFiles(directoryInput.file)
                .forEach(Consumer { file: File ->
                    biConsumer.accept(
                        file,
                        Status.ADDED
                    )
                })
        }
    }

    private fun getAllFiles(@NonNull dir: File): FluentIterable<File> {
        return FluentIterable.from(Files.fileTraverser().depthFirstPreOrder(dir))
            .filter(Files.isFile())
    }

    private fun eachJar(
        transformInvocation: TransformInvocation,
        isIncremental: Boolean,
        jarInput: JarInput
    ) {
        //File temDir = transformInvocation.getContext().getTemporaryDir();
        val createJarOutputName = createJarOutputName(jarInput)
        val dest = transformInvocation.outputProvider.getContentLocation(
            createJarOutputName,
            jarInput.contentTypes,
            jarInput.scopes,
            Format.JAR
        )
        val status: Status = if (isIncremental) {
            jarInput.status
        } else {
            Status.ADDED
        }
        when (status) {
            Status.NOTCHANGED -> {}
            Status.REMOVED -> if (dest.exists()) {
                FileUtils.forceDelete(dest)
            }
            Status.ADDED, Status.CHANGED -> {
                try {
                    FileUtils.touch(dest)
                } catch (e: Throwable) {
                    val pr = dest.parentFile
                    if (!pr.exists()) {
                        pr.mkdirs()
                    }
                }
                tasks.add(Callable<Unit> { mainSingleJarToFile(jarInput.file, dest) })
            }
        }
        Log.e("foreach Jar status=${status} jarInput.file=${jarInput.file.absolutePath} \njarOutput.file=${dest.absolutePath} createJarOutputName = ${createJarOutputName}")
    }

    private fun createJarOutputName(input: JarInput): String {
        var destName = input.name
        // 重新命名
        val hexName = DigestUtils.md5Hex(input.file.absolutePath)
        if (destName.endsWith(".jar")) {
            // 如果是.jar结尾，截取之前的字符串
            destName = destName.substring(0, destName.length - 4)
        }
        return destName + "_" + hexName
    }

    open fun canTransForm(name: String): Boolean {
        if (inputTypes.isEmpty()) return false
        return if (inputTypes.size == 1 && inputTypes == TransformManager.CONTENT_CLASS) {
            //如果只关注class，则过滤
            name.endsWith(".class")
        } else {
            //关注除了class其他的，由插件自行判断
            true
        }
    }

    // 是否移除jar 内部的 entry
    open fun isRemoveJarEntry(jarFile: JarFile, entry: JarEntry): Boolean {
        //Log.e("isRemoveJarEntry > ${jarFile.name} , ${entry.name}")
        return false
    }

    @Throws(IOException::class)
    private fun mainSingleJarToFile(inputFile: File, outputFile: File) {
        try {
            //存在 强制删除
            if (outputFile.exists()) {
                FileUtils.forceDelete(outputFile)
            }
            // 检查是否需要过滤jar
            if (isFilterJar(inputFile)) {
                FileUtils.copyFile(inputFile, outputFile)
                return
            }
            // 装换为jarfile
            val jarFile = JarFile(inputFile)
            val jarOutputStream = JarOutputStream(FileOutputStream(outputFile))
            // 获取并遍历
            val enumeration = jarFile.entries()
            while (enumeration.hasMoreElements()) {
                val entry = enumeration.nextElement()
                val name = entry.name // 打印的是每一个class文件
                if (isRemoveJarEntry(jarFile, entry)) {
                    continue
                }
                // 创建输出的jar.entry文件
                val outJarEntry = JarEntry(name)
                jarOutputStream.putNextEntry(outJarEntry)
                var outputEntryClassBytes: ByteArray? = null
                val inputEntryClassBytes = IOUtils.toByteArray(jarFile.getInputStream(entry))
                if (canTransForm(name)) {
                    outputEntryClassBytes = try {
                        transformJar(inputEntryClassBytes, entry, inputFile)
                    } catch (e: Throwable) {
                        Log.e("weaveSingleJarToFile error ${e}")
                        e.printStackTrace()
                        inputEntryClassBytes
                    }
                }
                outputEntryClassBytes?.also {
                    jarOutputStream.write(it)
                } ?: also {
                    jarOutputStream.write(inputEntryClassBytes)
                }
                jarOutputStream.flush()
                jarOutputStream.closeEntry()
            }
            jarOutputStream.close()
            jarFile.close()
        } catch (e: Throwable) {
            Log.e("weaveSingleJarToFile Throwable $e")
        }
    }

    @Throws(IOException::class)
    private fun mainSingleClassToFile(inputFile: File, outputFile: File) {
        // 判断师傅可以进行转变
        if (canTransForm(inputFile.name)) {
            //  创建文件，如果文件存在则更新时间；如果不存在，创建一个空文件
            FileUtils.touch(outputFile)
            // 读取输入文件为byteArray
            val inputClassBytes = FileUtils.readFileToByteArray(inputFile)
            val outputClassBytes = transform(inputClassBytes, inputFile)
            FileUtils.writeByteArrayToFile(outputFile, outputClassBytes)
        } else {
            // 直接进行复制即可
            if (inputFile.isFile) {
                FileUtils.touch(outputFile)
                FileUtils.copyFile(inputFile, outputFile)
            }
        }
    }

    open fun isFilterJar(jarFile: File): Boolean {
        return false
    }

    abstract fun transform(classBytes: ByteArray, classFile: File): ByteArray

    abstract fun transformJar(classBytes: ByteArray, entry: JarEntry, jarFile: File): ByteArray

}