package com.atom.compiler.apt.utils

import com.atom.compiler.apt.ext.replaceAll
import java.io.File

object ClassUtils {
    fun getClassFilePath(clazz: Class<*>): String {
        val dir = clazz.protectionDomain.codeSource.location.file
        val name = clazz.simpleName + ".class"
        val file = File(dir + clazz.`package`.name.replaceAll("[.]", "/") + "/", name)
        return file.absolutePath
    }
}