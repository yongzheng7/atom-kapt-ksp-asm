package com.atom.compiler.apt.ext

import com.atom.compiler.apt.utils.ClassUtils

fun Class<*>.getPath(): String {
    return ClassUtils.getClassFilePath(this)
}