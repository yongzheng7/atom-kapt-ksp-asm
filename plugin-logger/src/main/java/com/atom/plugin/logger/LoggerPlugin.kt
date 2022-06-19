package com.atom.plugin.logger

import com.android.build.gradle.AppExtension
import com.atom.plugin.core.AbstractPlugin
import com.atom.plugin.core.Log
import com.atom.plugin.logger.ReflectUtil.findField
import com.atom.plugin.logger.ReflectUtil.getMethod
import com.atom.plugin.logger.compile.AutotrackBuildException
import com.atom.plugin.logger.compile.ClassRewriter
import com.sun.org.apache.xpath.internal.operations.Bool
import org.gradle.api.Project
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URL
import java.net.URLDecoder
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.regex.Matcher
import java.util.regex.Pattern


class LoggerPlugin : AbstractPlugin<LoggerExtension>() {

    companion object {
        const val sdkName = "io.github.yongzheng7:module-logger"
        const val sdkVersion = "7.0.2"
    }

    override fun getExtensionClass(): Class<LoggerExtension> {
        return LoggerExtension::class.java
    }

    override fun getExtensionName(): String {
        return this.javaClass.simpleName
    }

    override fun afterEvaluate(project: Project, app: AppExtension) {
        this.extension?.enableUse = checkLoggerDependency(project)
        super.afterEvaluate(project, app)
    }

    override fun transformJar(
        classBytes: ByteArray,
        entry: JarEntry,
        inputFile: File,
        outputFile: File
    ): ByteArray {
        return transformDir(classBytes, inputFile, outputFile)
    }

    override fun transformDir(classBytes: ByteArray, inputFile: File, outputFile: File): ByteArray {
        return extension?.let { ext ->
            if (!ext.enableUse) {
                return classBytes
            }
            if(!inputFile.absolutePath.contains("com\\atom\\bytecode")){
                return classBytes
            }
            Log.e("${getExtensionName()} absolutePath = ${inputFile.absolutePath} ")
            val reader = ClassReader(classBytes)
            val writer = ClassWriter(1)
            reader.accept(
                HookClassVisitor(reader.className, Opcodes.ASM5, writer),
                ClassReader.EXPAND_FRAMES
            )
            writer.toByteArray()
        } ?: classBytes
    }

    private fun checkLoggerDependency(project: Project): Boolean {
        for (configuration in project.configurations) {
            //checkAutotrackDependency configuration = releaseRuntimeOnly
            //Log.e("checkAutotrackDependency configuration = ${configuration.name}")
            if ("releaseRuntimeClasspath" == configuration.name) {
                Log.e("releaseRuntimeClasspath plugin version = ")
                for (dependency in configuration.incoming.resolutionResult.root.dependencies) {
                    /**
                     * checkAutotrackDependency dependency =project :module-core
                     * from = project :app
                     * getRequested = project :module-core
                     * getRequested.displayName = project :module-core
                     */
                    val bb: (DependencyResult) -> Boolean = { sdk ->
                        //Log.e("checkAutotrackDependency ->\n dependency =${sdk} \n from = ${sdk.from} \n getRequested = ${sdk.getRequested()}")
                        (getSdkName(sdk).startsWith(sdkName)).also {
                            if (it) {
                                Log.e("checkAutotrackDependency ->找到依赖了")
                            }
                        } && (getSdkVersion(sdk) == sdkVersion).also {
                            if (it) {
                                Log.e("checkAutotrackDependency ->版本正确")
                            } else {
                                Log.e("checkAutotrackDependency ->版本错误")
                            }
                        }
                    }
                    if (checkDependency(dependency, bb)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun checkDependency(
        dependency: DependencyResult,
        block: (DependencyResult) -> Boolean
    ): Boolean {
        if (block.invoke(dependency)) {
            return true
        }
        if (dependency is ResolvedDependencyResult) {
            for (result in dependency.selected.dependencies) {
                if (checkDependency(result, block)) {
                    return true
                }
            }
        }
        return false
    }

    private fun getSdkVersion(sdk: DependencyResult): String {
        return getSdkName(sdk).split(":")[2]
    }

    private fun getSdkName(sdk: DependencyResult): String {
        return sdk.requested.displayName
    }

    private fun checkJavaVersion() {
        val version = System.getProperty("java.version")
        val matcher: Matcher = Pattern.compile("^(1\\.[0-9]+)\\..*").matcher(version)
        if (matcher.find()) {
            val versionNum: String = matcher.group(1)
            try {
                val num = (versionNum.toFloat() * 10).toInt()
                if (num < 18) {
                    throw RuntimeException("GrowingIO autotracker gradle plugin 要求编译环境的JDK为1.8及以上")
                }
            } catch (e: NumberFormatException) {
                // ignore
            }
            return
        }
        Log.e("GIO: check java version failed")
    }

    private fun getPluginVersion(): String {
        try {
            val jarPath: String =
                URLDecoder.decode(File(ClassRewriter::class.java.protectionDomain.codeSource.location.path).canonicalPath)
            JarInputStream(FileInputStream(jarPath)).use { inputStream ->
                Log.e("getPluginVersion ${jarPath} ,  ${inputStream.manifest.mainAttributes}")
                inputStream.manifest.mainAttributes.forEach { t, u ->
                    Log.e("getPluginVersion ${t} ,  ${u}")
                }
                return inputStream.manifest.mainAttributes
                    .getValue("Gradle-Plugin-Version")
                    ?: "Cannot find GrowingIO autotrack gradle plugin version"
            }
        } catch (e: IOException) {
            Log.e("getPluginVersion error ${e}")
            return "Cannot find GrowingIO autotrack gradle plugin version"
        }
    }

    private fun onGotAndroidJarFiles(appExtension: AppExtension): List<URL> {
        //checkJackStatus(appExtension)
        try {
            val files = appExtension.bootClasspath
            val androidJars: MutableList<URL> = ArrayList()
            for (file in files) {
                androidJars.add(file.toURL())
            }
            return androidJars
        } catch (e: Exception) {
            e.printStackTrace()
            throw java.lang.RuntimeException("GIO: get android.jar failed")
        }
    }

    private fun checkJackStatus(appExtension: AppExtension) {
        // 高版本Gradle没有这个特性了
        // config.getJackOptions().isEnabled();
        var errorMessage: String? = null
        try {
            val configMethod =
                getMethod(AppExtension::class.java, "getDefaultConfig")
                    ?: return
            val config = configMethod.invoke(appExtension)
            val jackOptions = findField<Any>(config, "jackOptions")
                ?: return
            val isEnable = getMethod(jackOptions.javaClass, "isEnabled")
                ?: return
            val jackEnabled = isEnable.invoke(jackOptions) as Boolean
            if (jackEnabled) {
                errorMessage = """
                ========= GIO无埋点SDK不支持Jack编译器
                ========= 由于TransformApi不支持Jack编译器且Jack项目已被Google废弃。请确保没有以下配置:
                ========= jackOptions {
                =========       enabled true
                ========= }
                
                """.trimIndent()
            }
        } catch (e: java.lang.Exception) {
            // ignore
            println(e.message)
        }
    }

    private class HookClassVisitor(val className: String, api: Int, cv: ClassVisitor) :
        ClassVisitor(api, cv) {

        override fun visitMethod(
            access: Int, // 标志位 1 override /17 default / 18 private / 20 protected
            name: String?, // 方法名称
            descriptor: String?, // 形参和返回 ()Ljava/lang/String; 雷同 jni注册方法
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor {
            Log.e("HookClassVisitor visitMethod  , access =${access} ,name= ${name}  ,descriptor =${descriptor} ,signature= ${signature} ,exceptions= ${ exceptions} ")
            val visitMethod = super.visitMethod(access, name, descriptor, signature, exceptions)
            name ?: return visitMethod
            if (name == "<init>") return visitMethod
            return HookMethodVisitor(className, name, Opcodes.ASM5, visitMethod)
        }
    }

    private class HookMethodVisitor(
        val className: String,
        val methodName: String,
        api: Int,
        mv: MethodVisitor
    ) : MethodVisitor(api, mv), Opcodes {
        // 属于方法的开始
        override fun visitCode() {
            super.visitCode()
            addLogger(true)
        }

        override fun visitInsn(opcode: Int) {
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                addLogger(false)
            }
            super.visitInsn(opcode)
        }

        private fun addLogger(isStart: Boolean) {
            mv.visitFieldInsn(
                Opcodes.GETSTATIC,
                "com/atom/module/logger/Logger",
                "Forest",
                "Lcom/atom/module/logger/Logger\$Forest;"
            )
            mv.visitInsn(Opcodes.ICONST_3)
            mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object")
            mv.visitVarInsn(Opcodes.ASTORE, 3)
            mv.visitVarInsn(Opcodes.ALOAD, 3)
            mv.visitInsn(Opcodes.ICONST_0)
            mv.visitLdcInsn(className)
            mv.visitInsn(Opcodes.AASTORE)
            mv.visitVarInsn(Opcodes.ALOAD, 3)
            mv.visitInsn(Opcodes.ICONST_1)
            mv.visitLdcInsn(methodName)
            mv.visitInsn(Opcodes.AASTORE)
            mv.visitVarInsn(Opcodes.ALOAD, 3)
            mv.visitInsn(Opcodes.ICONST_2)
            mv.visitLdcInsn(if (isStart) "start--------------------->" else "end---------------------<")
            mv.visitInsn(Opcodes.AASTORE)
            mv.visitVarInsn(Opcodes.ALOAD, 3)
            mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "com/atom/module/logger/Logger\$Forest",
                "i",
                "([Ljava/lang/Object;)V",
                false
            )
        }
    }
}