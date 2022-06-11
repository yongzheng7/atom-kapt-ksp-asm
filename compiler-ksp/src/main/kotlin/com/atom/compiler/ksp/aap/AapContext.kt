package com.atom.compiler.ksp.aap

import com.atom.compiler.ksp.core.KspContext
import com.atom.compiler.ksp.core.KspLog
import com.atom.compiler.ksp.ext.upperFirstLetter
import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

class AapContext(val context: KspContext, options: Map<String, String>) {

    val moduleName: String
    val isDebug: Boolean
    val classSet = HashSet<KSClassDeclaration>()

    @Suppress("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    init {
        // option debug
        isDebug = "true".equals(options[AapOptions.DEBUG_OPTION], ignoreCase = true)
        KspLog.debug = isDebug
        // option module
        val bundleModuleName = options[AapOptions.BUNDLE_OPTION]?.let {
            it.ifEmpty {
                UUID.randomUUID().toString()
            }
        } ?: let {
            UUID.randomUUID().toString()
        }
        moduleName = bundleModuleName.replace("[^0-9a-zA-Z_]+", "").upperFirstLetter()
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