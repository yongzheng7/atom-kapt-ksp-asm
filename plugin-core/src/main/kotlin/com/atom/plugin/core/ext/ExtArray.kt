package com.atom.plugin.core.ext

import java.lang.StringBuilder


fun <T> Array<T>.printString(): String {
    if (this.isEmpty()) return ""
    val result = StringBuilder()
    this.forEachIndexed { index, t ->
        result.append("[$index=$t]").append(",")
    }
    return result.toString()
}