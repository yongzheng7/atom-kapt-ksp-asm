package com.atom.plugin.logger

import com.android.build.gradle.AppExtension
import com.atom.plugin.core.AbstractPlugin
import com.atom.plugin.core.Log
import com.atom.plugin.logger.ReflectUtil.findField
import com.atom.plugin.logger.ReflectUtil.getMethod
import com.atom.plugin.logger.compile.AutotrackBuildException
import com.atom.plugin.logger.compile.AutotrackTransform
import com.atom.plugin.logger.compile.ClassRewriter
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
    override fun getExtensionClass(): Class<LoggerExtension> {
        return LoggerExtension::class.java
    }

    override fun getExtensionName(): String {
        return this.javaClass.simpleName
    }

    override fun transformDir(classBytes: ByteArray, inputFile: File, outputFile: File): ByteArray {
        return classBytes
    }

    override fun afterEvaluate(project: Project) {
        super.afterEvaluate(project)
        checkJavaVersion()
        checkAutotrackDependency(project);
       // onGotAndroidJarFiles(android);
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

    private fun checkAutotrackDependency(project: Project) {
        for (configuration in project.configurations) {
            if ("releaseRuntimeClasspath" == configuration.name) {
                for (dependency in configuration.incoming.resolutionResult.root.dependencies) {
                    if (findAutotrackDependency(dependency)) {
                        return
                    }
                }
            }
        }
        throw java.lang.RuntimeException("未发现autotrack依赖，请参考官方文档添加依赖")
    }

    private fun findAutotrackDependency(dependency: DependencyResult): Boolean {
        val autotrackDependency = "com.growingio.android:autotracker-core:"
        if (dependency.getRequested().getDisplayName().startsWith(autotrackDependency)) {
            val sdkVersion: String = dependency.getRequested().getDisplayName().split(":").get(2)
            val pluginVersion: String = getPluginVersion()
            return if (sdkVersion == pluginVersion) {
                true
            } else {
                throw AutotrackBuildException("您的autotracker-gradle-plugin版本号[$pluginVersion]和autotracker版本号[$sdkVersion]不一致，请在build.gradle文件中修改")
            }
        }
        if (dependency is ResolvedDependencyResult) {
            for (result in (dependency as ResolvedDependencyResult).selected.dependencies) {
                if (findAutotrackDependency(result)) {
                    return true
                }
            }
        }
        return false
    }

    fun getPluginVersion(): String {
        try {
            val jarPath: String = URLDecoder.decode(File(ClassRewriter::class.java.protectionDomain.codeSource.location.path).canonicalPath)
            JarInputStream(FileInputStream(jarPath)).use { inputStream ->
                return inputStream.manifest.mainAttributes
                    .getValue("Gradle-Plugin-Version")
                    ?: throw AutotrackBuildException("Cannot find GrowingIO autotrack gradle plugin version")
            }
        } catch (e: IOException) {
            throw AutotrackBuildException("Cannot find GrowingIO autotrack gradle plugin version")
        }
    }

    private fun onGotAndroidJarFiles(appExtension: AppExtension):List<URL> {
        checkJackStatus(appExtension)
        try {
            val files = appExtension.bootClasspath
            if (files.isEmpty()) {
                throw java.lang.RuntimeException("GIO: get android.jar failed")
            }
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