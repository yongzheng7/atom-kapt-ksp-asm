package com.atom.compiler.apt.aap

import com.atom.compiler.apt.core.AptContext
import com.atom.compiler.apt.core.AptLog
import com.atom.compiler.apt.ext.upperFirstLetter
import java.text.SimpleDateFormat
import java.util.*
import javax.lang.model.element.TypeElement
import kotlin.collections.HashSet

class AapContext(val context: AptContext, options: Map<String, String>) {

    val moduleName: String
    val isDebug: Boolean
    val classSet = HashSet<TypeElement>()

    @Suppress("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

    init {
        // option debug
        isDebug = "true".equals(options[AapOptions.DEBUG_OPTION], ignoreCase = true)
        AptLog.debug = isDebug
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