package com.atom.plugin.logger

import com.atom.plugin.core.AbstractExtension

open class LoggerExtension : AbstractExtension(){

    var hookPackets: Array<String>? = null

    var hookClasses: Array<String>? = null

    override fun toString(): String {
        return "LoggerExtension(hookPackets=${hookPackets?.contentToString()}, hookClasses=${hookClasses?.contentToString()})"
    }

}