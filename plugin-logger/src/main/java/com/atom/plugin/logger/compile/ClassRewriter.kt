package com.atom.plugin.logger.compile

import com.atom.plugin.core.Log
import com.atom.plugin.logger.visiter.*

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class ClassRewriter {

    private val mLog: Log
    private val mClassLoader: ClassLoader
    private val mUserExcludePackages: List<String>
    private var mExcludeOfficial = false
    private val EXCLUDED_PACKAGES = arrayOf(
        "com/growingio/android/",
        "com/growingio/giokit/",
        "com/alibaba/mobileim/extra/xblink/webview",
        "com/alibaba/sdk/android/feedback/xblink",
        "com/tencent/smtt",
        "com/baidu/location",
        "com/blueware/agent/android",
        "com/oneapm/agent/android",
        "com/networkbench/agent",
        "android/taobao/windvane/webview"
    )

    private val OFFICIAL_PACKAGES = arrayOf(
        "android/arch/",
        "com/google/",
        "javax/",
        "io/rectivex/rxjava",
        "org/jetbrains/kotlin"
    )


    constructor(
        log: Log,
        classLoader: ClassLoader,
        userExcludePackages: Array<String>?,
        excludeOfficial: Boolean
    ) {
        mLog = log
        mClassLoader = classLoader
        mExcludeOfficial = excludeOfficial
        if (userExcludePackages == null) {
            mUserExcludePackages = emptyList()
        } else {
            mUserExcludePackages = arrayListOf()
            for (i in userExcludePackages.indices) {
                mUserExcludePackages[i] = userExcludePackages[i].replace(".", "/")
            }
        }
    }

    private fun isExcludedPackage(packageName: String): Boolean {
        for (exPackage in EXCLUDED_PACKAGES) {
            if (packageName.startsWith(exPackage)) {
                return true
            }
        }
        for (exPackage in mUserExcludePackages) {
            if (packageName.startsWith(exPackage)) {
                return true
            }
        }
        if (!mExcludeOfficial) return false
        for (exPackage in OFFICIAL_PACKAGES) {
            if (packageName.startsWith(exPackage)) {
                return true
            }
        }
        return false
    }

    fun transformClass(from: InputStream?, to: OutputStream?): Boolean {
        try {
            val bytes: ByteArray = IOUtils.toByteArray(from)
            val modifiedClass = visitClassBytes(bytes)
            if (modifiedClass != null) {
                IOUtils.write(modifiedClass, to)
                return true
            } else {
                IOUtils.write(bytes, to)
            }
        } catch (e: IOException) {
            mLog.e(" ${e.localizedMessage} \n $e")
        }
        return false
    }

    fun transformClassFile(from: File?, to: File): Boolean {
        var result = false
        val toParent: File = to.getParentFile()
        toParent.mkdirs()
        try {
            FileInputStream(from).use { fileInputStream ->
                FileOutputStream(to).use { fileOutputStream ->
                    result = transformClass(fileInputStream, fileOutputStream)
                }
            }
        } catch (e: Exception) {
            mLog.e(" ${e.localizedMessage} \n $e")
            result = false
        }
        return result
    }

    private fun visitClassBytes(bytes: ByteArray): ByteArray? {
        var className: String? = null
        try {
            val classReader = ClassReader(bytes)
            var classWriter = AutotrackClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
            val context = Context(mLog, mClassLoader)
            classReader.accept(
                ContextClassVisitor(classWriter.getApi(), context),
                ClassReader.SKIP_DEBUG or ClassReader.SKIP_CODE
            )
            className = context.getClassName()
            val classVisitor: ClassVisitor
            if (className == null || context.getClassName() == null || isExcludedPackage(context.getClassName()!!)) {
                return null
            }
            val desugaringClassVisitor = DesugaringClassVisitor(
                classWriter.getApi(),
                InjectAroundClassVisitor(
                    classWriter.getApi(),
                    InjectSuperClassVisitor(classWriter.getApi(), classWriter, context),
                    context
                ),
                context
            )
            classVisitor = desugaringClassVisitor
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
            if (!desugaringClassVisitor.getNeedInjectTargetMethods()!!.isEmpty()) {
                // lambda 表达式需要特殊处理两次
                mLog.w(String.format("GIO: deal with lambda second time:  %s", className))
                val lambdaReader = ClassReader(classWriter.toByteArray())
                classWriter = AutotrackClassWriter(lambdaReader, ClassWriter.COMPUTE_MAXS)
                lambdaReader.accept(
                    DesugaredClassVisitor(
                        classWriter.getApi(),
                        classWriter,
                        context,
                        desugaringClassVisitor.getNeedInjectTargetMethods()
                    ), ClassReader.EXPAND_FRAMES
                )
            }
            if (context.isClassModified()) {
                return classWriter.toByteArray()
            }
        } catch (e: AutotrackBuildException) {
            throw RuntimeException(e)
        } catch (t: Throwable) {
            mLog.e(
                """
                Unfortunately, an error has occurred while processing $className. Please copy your build logs and the jar containing this class and visit https://www.growingio.com, thanks!
                ${t.message}
                
                $t
                """.trimIndent(),
            )
        }
        return null
    }
}