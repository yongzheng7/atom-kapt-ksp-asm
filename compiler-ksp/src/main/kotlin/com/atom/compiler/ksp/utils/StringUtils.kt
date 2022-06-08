package com.atom.compiler.ksp.utils

import java.lang.StringBuilder

object StringUtils {

    fun <T> stringSequence(list: Sequence<T>, block: (T) -> String): String {
        val result = StringBuilder()
        list.forEach {
            result.append(block.invoke(it)).append("\n")
        }
        return result.toString()
    }

    fun <T> stringIterable(list: Iterable<T>, block: (T) -> String): String {
        val result = StringBuilder()
        list.forEach {
            result.append(block.invoke(it)).append("\n")
        }
        return result.toString()
    }
}