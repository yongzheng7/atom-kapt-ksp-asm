package com.atom.compiler.ksp.ext

import java.io.File
import java.util.regex.Pattern

fun String.upperFirstLetter(): String {
    if (this.isEmpty() || !Character.isLowerCase(this[0])) return this
    return ((this[0].code - 32).toChar() + this.substring(1))
}

fun String.replaceAll(regex: String, replacement: String): String {
    return Pattern.compile(regex).matcher(this).replaceAll(replacement)
}

fun String.createFile(): File {
    return File(this)
}