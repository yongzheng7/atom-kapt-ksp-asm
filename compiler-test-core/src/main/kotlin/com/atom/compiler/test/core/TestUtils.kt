package com.atom.compiler.test.core

import io.github.classgraph.ClassGraph
import java.io.File

fun defaultCompilerConfig(): KotlinCompilation {
    return KotlinCompilation().apply {
        inheritClassPath = false
        skipRuntimeVersionCheck = true
        correctErrorTypes = true
        verbose = true
        reportOutputFiles = false
        messageOutputStream = System.out
    }
}

fun defaultJsCompilerConfig(): KotlinJsCompilation {
    return KotlinJsCompilation().apply {
        inheritClassPath = false
        verbose = true
        reportOutputFiles = false
        messageOutputStream = System.out
    }
}


fun assertClassLoadable(compileResult: KotlinCompilation.Result, className: String): Class<*> {
    try {
        return compileResult.classLoader.loadClass(className)
    } catch (e: ClassNotFoundException) {
        throw e
    }
}

/**
 * Returns the classpath for a dependency (format $name-$version).
 * This is necessary to know the actual location of a dependency
 * which has been included in test runtime (build.gradle).
 */
fun classpathOf(dependency: String): File {
    val regex = Regex(".*$dependency\\.jar")
    return ClassGraph().classpathFiles.first { classpath -> classpath.name.matches(regex) }
}
