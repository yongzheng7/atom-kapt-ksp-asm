package com.atom.compiler.ksp.aap

import com.atom.compiler.ksp.core.KspContext
import com.atom.compiler.ksp.core.KspLog
import com.atom.compiler.ksp.core.KspProcessor
import com.atom.module.annotation.aap.AapImpl
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.*
import java.lang.StringBuilder


class AapSymbolProcessor(environment: SymbolProcessorEnvironment) : KspProcessor(environment) {

    class Provider : SymbolProcessorProvider{
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return AapSymbolProcessor(environment)
        }
    }

    override fun onProcess(kspContext: KspContext): List<KSAnnotated> {
        val aapContext = AapContext(kspContext, kspContext.environment.options)
        KspLog.warning("AapSymbolProcessor Version, ${KotlinVersion.CURRENT}")
        KspLog.warning("AapSymbolProcessor Options, ${kspContext.environment.options}")
        val apiImpls = kspContext.resolver.getSymbolsWithAnnotation(AapImpl::class.qualifiedName!!).filterIsInstance<KSClassDeclaration>()
        val result = mutableSetOf<AapMeta>()
        apiImpls.forEach {
            try {
                result.add(AapMeta.create(aapContext, it))
            } catch (e: Exception) {
                KspLog.error(e)
            }
        }
        try{
            AapMetas(aapContext).addMetasCode(result).assembleCode()
        }catch (e: Exception){
            KspLog.error(e)
        }
        return emptyList()
    }

    override fun onError() {
        super.onError()
        KspLog.info("${this.javaClass.simpleName} , onError() ")
    }

    override fun finish() {
        super.finish()
        KspLog.info("${this.javaClass.simpleName} , finish() ")
    }
}