package com.atom.plugin.core

import com.atom.plugin.core.ext.replaceAll
import java.io.File

object ClassUtils {
    fun getClassFilePath(clazz: Class<*>): String {
        val dir = clazz.protectionDomain.codeSource.location.file
        val name = clazz.simpleName + ".class"
        val file = File(dir + clazz.`package`.name.replaceAll("[.]", "/") + "/", name)
        return file.absolutePath
    }
}