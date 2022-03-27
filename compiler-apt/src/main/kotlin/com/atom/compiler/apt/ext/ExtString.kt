package com.atom.compiler.apt.ext

fun String.upperFirstLetter(): String {
    if (this.isEmpty() || !Character.isLowerCase(this[0])) return this
    return ((this[0].code - 32).toChar() + this.substring(1))
}