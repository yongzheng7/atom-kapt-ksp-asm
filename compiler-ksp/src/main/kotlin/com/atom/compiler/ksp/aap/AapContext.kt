package com.atom.compiler.ksp.aap

import com.atom.compiler.ksp.common.KspContext
import com.atom.compiler.ksp.common.KspLog
import com.atom.compiler.ksp.ext.upperFirstLetter
import java.text.SimpleDateFormat

class AapContext(val context: KspContext, options: Map<String, String>) {

    val moduleName: String
    val isDebug: Boolean
    val classSet = HashSet<String>()

    @Suppress("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    init {
        // option debug
        isDebug = "true".equals(options[AapOptions.DEBUG_OPTION], ignoreCase = true)
        KspLog.debug = isDebug
        // option module
        val bundleModuleName = options[AapOptions.BUNDLE_OPTION]?.also {
            if (it.isEmpty()) throw RuntimeException(">>> module name is empty")
        } ?: throw RuntimeException(">>> No module name")
        moduleName = bundleModuleName.replace("[^0-9a-zA-Z_]+", "").let {
            if (it.isEmpty()) throw RuntimeException(">>> module name is empty")
            it.upperFirstLetter()
        }
    }

    override fun toString(): String {
        return """
            
            ----------------------------------------------------------------------------------------
            AapContext
            context = $context
            moduleName = $moduleName 
            isDebug = $isDebug 
            classSet = $classSet
            ----------------------------------------------------------------------------------------
        """.trimIndent()
    }


}