package com.atom.compiler.ksp.core

import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

object KspContext {

    lateinit var environment: SymbolProcessorEnvironment
    lateinit var resolver: Resolver

    val builtIns: KSBuiltIns
        get() = resolver.builtIns

    fun init(environment: SymbolProcessorEnvironment, resolver: Resolver): KspContext {
        KspContext.environment = environment
        KspContext.resolver = resolver
        return this
    }
}