package com.atom.compiler.apt.aap

import com.atom.compiler.apt.common.AptContext
import com.atom.compiler.apt.common.AptLog
import com.atom.compiler.apt.ext.upperFirstLetter
import java.lang.RuntimeException

class AapContext(val context: AptContext, options: Map<String, String>) {

    val moduleName: String
    val isDebug: Boolean
    val classSet = HashSet<String>()

    init {
        // option debug
        isDebug = "true".equals(options[AapOptions.DEBUG_OPTION], ignoreCase = true)
        AptLog.debug = isDebug
        // option module
        val bundleModuleName = options[AapOptions.BUNDLE_OPTION]?.also {
            if (it.isEmpty()) throw RuntimeException(">>> module name is empty")
        } ?: throw RuntimeException(">>> No module name")
        moduleName = bundleModuleName.replace("[^0-9a-zA-Z_]+", "").let {
            if (it.isEmpty()) throw RuntimeException(">>> module name is empty")
            it.upperFirstLetter()
        }
    }

}