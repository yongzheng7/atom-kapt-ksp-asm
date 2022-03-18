package com.atom.compiler.ksp

import com.atom.compiler.ksp.common.KspContext
import com.atom.compiler.ksp.common.KspLog
import com.atom.module.aap.annotation.AapImpl
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import java.lang.StringBuilder


class AapSymbolProcessor(private val environment: SymbolProcessorEnvironment) :
    SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        KspContext.init(environment, resolver)
        KspLog.init(environment.logger, true)

        KspLog.warning("AapSymbolProcessor,process ------->>>>>>>> ${KotlinVersion.CURRENT}")

        try {
            AapImpl::class.qualifiedName?.also { s ->
                val symbolsWithAnnotation = resolver.getSymbolsWithAnnotation(s)
                symbolsWithAnnotation.forEach {
                    isKSClassDeclaration(it) { bean ->
                        KspLog.warning(
                            """
                            ${stringKSName(bean.qualifiedName)}      
                            ${bean.modifiers}
                            ${bean.typeParameters}
                            ${bean.primaryConstructor}
                            ${stringSequence(bean.getConstructors()) { b ->
                                stringKSFunctionDeclaration(b)
                            }
                            }
                        """.trimIndent()
                        )
                    }
                }

            }
        } catch (e: Exception) {
            KspLog.error(e)
        }
        KspLog.warning("AapSymbolProcessor,process -------<<<<<<<<< ${KotlinVersion.CURRENT}")
        return emptyList()
    }

    fun stringKSFunctionDeclaration(item: KSFunctionDeclaration): String {
        return stringIterable(item.parameters) { b ->
            stringKSValueParameter(b)
        }
    }

    fun stringKSValueParameter(item: KSValueParameter): String {
        return "${stringKSName(item.name)} ${item.type}"
    }

    fun isKSClassDeclaration(item: KSAnnotated, block: (KSClassDeclaration) -> Unit) {
        if (item is KSClassDeclaration) {
            block.invoke(item)
        }
    }

    fun stringKSAnnotated(item: KSAnnotated): String {
        return """
                
            location    = ${item.location}
            origin      = ${item.origin}
            parent      = ${item.parent}
            annotations = [${
            stringSequence(item.annotations) { aa: KSAnnotation ->
                stringKSAnnotations(aa)
            }
        }]
        """.trimIndent()
    }

    fun stringKSAnnotations(item: KSAnnotation): String {
        return """
                
            location            = ${item.location}
            origin              = ${item.origin}
            parent              = ${item.parent}
            annotationType      = ${item.annotationType}
            useSiteTarget       = ${item.useSiteTarget}
            shortName           = ${item.shortName}
            arguments           = [${
            stringIterable(item.arguments) { aa: KSValueArgument ->
                stringKSValueArguments(aa)
            }
        }]
        """.trimIndent()
    }

    fun stringKSValueArguments(item: KSValueArgument): String {
        return """
                
            location        = ${item.location}
            origin          = ${item.origin}
            parent          = ${item.parent}
            isSpread        = ${item.isSpread}
            name            = ${stringKSName(item.name)}
            value           = ${item.value.toString()}
            annotations     = [${
            stringSequence(item.annotations) { aa: KSAnnotation ->
                stringKSAnnotations(aa)
            }
        }]
        """.trimIndent()
    }

    fun stringKSName(item: KSName?): String {
        return "${item?.asString()}"
    }

    fun <T> stringSequence(list: Sequence<T>, block: (T) -> String): String {
        val result = StringBuilder()
        list.forEach {
            result.append(block.invoke(it)).append("\n")
        }
        return result.toString()
    }

    fun <T> stringIterable(list: Iterable<T>, block: (T) -> String): String {
        val result = StringBuilder()
        list.forEach {
            result.append(block.invoke(it)).append("\n")
        }
        return result.toString()
    }
}