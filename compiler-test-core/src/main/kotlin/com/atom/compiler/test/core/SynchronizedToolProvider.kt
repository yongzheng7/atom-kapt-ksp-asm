package com.atom.compiler.test.core

import com.atom.compile.core.isJdk9OrLater
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import javax.tools.JavaCompiler
import javax.tools.ToolProvider


/**
 * ToolProvider has no synchronization internally, so if we don't synchronize from the outside we
 * could wind up loading the compiler classes multiple times from different class loaders.
 */
internal object SynchronizedToolProvider {
    private var getPlatformClassLoaderMethod: Method? = null

    val systemJavaCompiler: JavaCompiler
        get() {
            val compiler = synchronized(ToolProvider::class.java) {
                ToolProvider.getSystemJavaCompiler()
            }

            check(compiler != null) { "System java compiler is null! Are you running without JDK?" }
            return compiler
        }

    // The compiler classes are loaded using the platform class loader in Java 9+.
    val systemToolClassLoader: ClassLoader
        get() {
            if (isJdk9OrLater()) {
                try {
                    return getPlatformClassLoaderMethod!!.invoke(null) as ClassLoader
                } catch (e: IllegalAccessException) {
                    throw RuntimeException(e)
                } catch (e: InvocationTargetException) {
                    throw RuntimeException(e)
                }

            }

            val classLoader: ClassLoader
            synchronized(ToolProvider::class.java) {
                classLoader = ToolProvider.getSystemToolClassLoader()
            }
            return classLoader
        }

    init {
        if (isJdk9OrLater()) {
            try {
                getPlatformClassLoaderMethod = ClassLoader::class.java.getMethod("getPlatformClassLoader")
            } catch (e: NoSuchMethodException) {
                throw RuntimeException(e)
            }

        }
    }
}