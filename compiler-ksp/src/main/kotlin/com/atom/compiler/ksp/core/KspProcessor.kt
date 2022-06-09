package com.atom.compiler.ksp.core

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import java.util.*

abstract class KspProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    var isProcess: Boolean = false

    final override fun process(resolver: Resolver): List<KSAnnotated> {
        if (isProcess) return emptyList()
        isProcess = true
        KspLog.init(environment.logger, true)
        return onProcess(KspContext.init(environment, resolver))
    }

    abstract fun onProcess(kspContext: KspContext): List<KSAnnotated>
}