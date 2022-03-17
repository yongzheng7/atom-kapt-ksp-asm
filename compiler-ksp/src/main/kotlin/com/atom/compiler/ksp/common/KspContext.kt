package com.atom.compiler.ksp.common

import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import javax.annotation.processing.ProcessingEnvironment

object KspContext {

    lateinit var environment: SymbolProcessorEnvironment
    lateinit var resolver: Resolver

    val builtIns: KSBuiltIns
        get() = resolver.builtIns

    fun init(environment: SymbolProcessorEnvironment, resolver: Resolver) {
        this.environment = environment
        this.resolver = resolver
    }
}