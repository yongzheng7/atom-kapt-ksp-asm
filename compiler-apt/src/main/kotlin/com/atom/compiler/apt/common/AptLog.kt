package com.atom.compiler.apt.common

import java.lang.StringBuilder
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: AptLog
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/3/9
 * @Copyright: 2022/3/9 www.rongdasoft.com Inc. All rights reserved.
 */
object AptLog {
    var msg: Messager? = null
    var debug = false

    fun init(msg: Messager, isDebug: Boolean) {
        AptLog.msg = msg
        debug = isDebug
    }

    fun info(info: String) {
        if (info.isNotEmpty() && debug) {
            msg?.printMessage(
                Diagnostic.Kind.NOTE, "[AptLog] $info"
            )
            println("[AptLog] $info")
        }
    }

    fun error(error: String) {
        if (error.isNotEmpty() && debug) {
            msg?.printMessage(
                Diagnostic.Kind.ERROR,
                "[AptLog] An exception is encountered, [$error]\n"
            )
            println("[AptLog] An exception is encountered, [$error]\n")
        }
    }

    fun error(error: String, var3: Element?) {
        if (error.isNotEmpty() && var3 != null && debug) {
            msg?.printMessage(
                Diagnostic.Kind.ERROR,
                "[AptLog] An exception is encountered, [$error]\n", var3
            )
            println("[AptLog] An exception is encountered, [$error]\n")
        }
    }

    fun error(error: Throwable?) {
        if (null != error && debug) {
            msg?.printMessage(
                Diagnostic.Kind.ERROR,
                """[AptLog] An exception is encountered, [${error.message}]
                ${formatStackTrace(error.stackTrace)}
                """.trimIndent()
            )
            println("""[AptLog] An exception is encountered, [${error.message}]
                ${formatStackTrace(error.stackTrace)}
                """.trimIndent())
        }
    }

    fun warning(warning: CharSequence?) {
        if (!warning.isNullOrEmpty() && debug) {
            msg?.printMessage(Diagnostic.Kind.WARNING, "[AptLog] $warning")
            println("[AptLog] $warning")
        }
    }

    private fun formatStackTrace(stackTrace: Array<StackTraceElement>): String {
        val sb = StringBuilder()
        for (element in stackTrace) {
            sb.append("    at ").append(element.toString())
            sb.append("\n")
        }
        return sb.toString()
    }
}