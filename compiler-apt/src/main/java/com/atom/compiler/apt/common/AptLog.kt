package com.atom.compiler.apt.common

import java.lang.StringBuilder
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
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
        this.msg = msg
        this.debug = isDebug
    }

    fun info(info: String) {
        if (info.isNotEmpty() && debug) {
            msg!!.printMessage(
                Diagnostic.Kind.NOTE, """
     $info
     
     """.trimIndent()
            )
        }
    }

    fun error(error: String) {
        if (error.isNotEmpty() && debug) {
            msg!!.printMessage(
                Diagnostic.Kind.ERROR,
                "An exception is encountered, [$error]\n"
            )
        }
    }

    fun error(error: String, var3: Element?) {
        if (error.isNotEmpty() && var3 != null && debug) {
            msg?.printMessage(
                Diagnostic.Kind.ERROR,
                "An exception is encountered, [$error]\n", var3
            )
        }
    }

    fun error(error: Throwable?) {
        if (null != error && debug) {
            msg?.printMessage(
                Diagnostic.Kind.ERROR,
                """
                An exception is encountered, [${error.message}]
                ${formatStackTrace(error.stackTrace)}
                """.trimIndent()
            )
        }
    }

    fun warning(warning: CharSequence?) {
        if (!warning.isNullOrEmpty() && debug) {
            msg?.printMessage(Diagnostic.Kind.WARNING, warning)
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