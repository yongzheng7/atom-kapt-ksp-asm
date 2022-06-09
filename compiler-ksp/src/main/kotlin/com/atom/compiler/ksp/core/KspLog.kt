package com.atom.compiler.ksp.core

import com.google.devtools.ksp.processing.KSPLogger
import java.lang.StringBuilder
import java.util.*
import javax.lang.model.element.Element

/**
 * All rights Reserved, Designed By www.rongdasoft.com
 * @version V1.0
 * @Title: KspLog
 * @Description:
 * @author: wuyongzheng
 * @date: 2022/3/11
 * @Copyright: 2022/3/11 www.rongdasoft.com Inc. All rights reserved.
 */
object KspLog {


    var msg: KSPLogger? = null
    var debug = false

    fun init(msg: KSPLogger, isDebug: Boolean) {
        KspLog.msg = msg
        debug = isDebug
    }

    fun info(info: String) {
        if (info.isNotEmpty() && debug) {
            msg?.info(info)
            println(info)
        }
    }

    fun error(error: String) {
        if (error.isNotEmpty() && debug) {
            msg?.error(
                "An exception is encountered, [$error]\n"
            )
            println("An exception is encountered, [$error]\n")
        }
    }

    fun error(error: String, var3: Element?) {
        if (error.isNotEmpty() && var3 != null && debug) {
            msg?.error(
                "An exception is encountered, [$error]\n  $var3"
            )
            println("An exception is encountered, [$error]\n  $var3")
        }
    }

    fun error(error: Throwable?) {
        if (null != error && debug) {
            msg?.error(
                """
                An exception is encountered, [${error.message}]
                ${formatStackTrace(error.stackTrace)}
                """.trimIndent()
            )
            println("""
                An exception is encountered, [${error.message}]
                ${formatStackTrace(error.stackTrace)}
                """.trimIndent())
        }
    }

    fun warning(warning: CharSequence?) {
        if (!warning.isNullOrEmpty() && debug) {
            msg?.warn(warning.toString())
            println(warning.toString())
        }
    }

    private fun formatStackTrace(stackTrace: Array<StackTraceElement>): String {
        val sb = StringBuilder()
        for (element in stackTrace) {
            sb.append("at").append(element.toString())
            sb.append("\n")
        }
        return sb.toString()
    }

}