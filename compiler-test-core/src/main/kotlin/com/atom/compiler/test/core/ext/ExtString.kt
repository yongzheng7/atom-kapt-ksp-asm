package com.atom.compiler.test.core.ext

import java.util.regex.Pattern

fun String.replaceAll(regex: String, replacement: String): String {
    return Pattern.compile(regex).matcher(this).replaceAll(replacement)
}