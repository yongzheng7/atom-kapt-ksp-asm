package com.atom.plugin.core.ext

import java.util.regex.Pattern

object UtilJvm {
    fun checkJavaVersion(): Int {
        val version = System.getProperty("java.version")
        val matcher = Pattern.compile("^(1\\.[0-9]+)\\..*").matcher(version)
        if (matcher.find()) {
            val versionNum = matcher.group(1)
            try {
                return (versionNum.toFloat() * 10).toInt()
            } catch (e: NumberFormatException) {
                // ignore
            }
        }
        return -1
    }
}