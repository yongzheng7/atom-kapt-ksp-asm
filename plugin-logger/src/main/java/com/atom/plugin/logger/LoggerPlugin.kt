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
        val sdkName = "androidx.core:core-ktx"
    }

    override fun getExtensionClass(): Class<LoggerExtension> {
        return LoggerExtension::class.java
    }

    override fun getExtensionName(): String {
        return this.javaClass.simpleName
    }

    override fun transformDir(classBytes: ByteArray, inputFile: File, outputFile: File): ByteArray {
        return classBytes
    }

    override fun afterEvaluate(project: Project, app: AppExtension) {
        super.afterEvaluate(project, app)
        checkJavaVersion()
        Log.e("checkLoggerDependency > ${checkLoggerDependency(project)}")
        Log.e("onGotAndroidJarFiles > ${onGotAndroidJarFiles(app)}")
    }

    override fun transformJar(
        classBytes: ByteArray,
        entry: JarEntry,
        inputFile: File,
        outputFile: File
    ): ByteArray {
        return classBytes
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


    private fun checkLoggerDependency(project: Project): Boolean {
        for (configuration in project.configurations) {
            //checkAutotrackDependency configuration = releaseRuntimeOnly
            //Log.e("checkAutotrackDependency configuration = ${configuration.name}")
            if ("releaseRuntimeClasspath" == configuration.name) {
                Log.e("releaseRuntimeClasspath plugin version = ${getPluginVersion()}")
                for (dependency in configuration.incoming.resolutionResult.root.dependencies) {
                    /**
                     * checkAutotrackDependency dependency =project :module-core
                     * from = project :app
                     * getRequested = project :module-core
                     * getRequested.displayName = project :module-core
                     */
                    val bb: (DependencyResult) -> Boolean = { sdk ->
                        Log.e("checkAutotrackDependency ->\n dependency =${sdk} \n from = ${sdk.from} \n getRequested = ${sdk.getRequested()}")
                        (getSdkName(sdk).startsWith(sdkName)).also {
                            if (it) {
                                Log.e("checkAutotrackDependency ->找到依赖了")
                            }
                        } && (getSdkVersion(sdk) == "1.3.0").also {
                            if (it) {
                                Log.e("checkAutotrackDependency ->找到版本了")
                            } else {
                                Log.e("checkAutotrackDependency ->找到版本了")
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

    fun getPluginVersion(): String {
        try {
            val jarPath: String =
                URLDecoder.decode(File(ClassRewriter::class.java.protectionDomain.codeSource.location.path).canonicalPath)
            JarInputStream(FileInputStream(jarPath)).use { inputStream ->
                Log.e("getPluginVersion ${jarPath} ,  ${inputStream.manifest.mainAttributes}")
                return inputStream.manifest.mainAttributes
                    .getValue("Gradle-Plugin-Version")
                    ?: "Cannot find GrowingIO autotrack gradle plugin version"
            }
        } catch (e: IOException) {
            Log.e("getPluginVersion error ${e}")
            throw AutotrackBuildException("Cannot find GrowingIO autotrack gradle plugin version")
        }
    }

    fun getSdkVersion(sdk: DependencyResult): String {
        return getSdkName(sdk).split(":")[2]
    }

    fun getSdkName(sdk: DependencyResult): String {
        return sdk.requested.displayName
    }

    private fun onGotAndroidJarFiles(appExtension: AppExtension): List<URL> {
        checkJackStatus(appExtension)
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

}