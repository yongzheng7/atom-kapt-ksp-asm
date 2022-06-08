package com.atom.compiler.ksp.aap

import com.atom.compiler.ksp.core.KspLog
import com.atom.compiler.ksp.ext.getClassCanonicalName
import com.atom.compiler.ksp.ext.hasPublicEmptyDefaultConstructor
import com.atom.module.annotation.aap.AapImpl
import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

class AapMeta {

    companion object {
        fun create(aapContext: AapContext, element: KSClassDeclaration): AapMeta {
            if (!element.isPublic()) {
                throw AapException("${element.qualifiedName} is not Public")
            }
            if (element.isAbstract()) {
                throw AapException("${element.qualifiedName} is Abstract")
            }
            if (!element.hasPublicEmptyDefaultConstructor()) {
                throw AapException("${element.qualifiedName} has not PublicEmptyDefaultConstructor")
            }
            return AapMeta(aapContext, element)
        }
    }

    val aapContext: AapContext
    val implTypeElement: KSClassDeclaration
    val apiTypeElement: KSClassDeclaration
    val implName: String
    val implVersion: Long
    val superTypeMap = mutableMapOf<String, KSType>()

    private constructor(aapContext: AapContext, element: KSClassDeclaration) {
        element.declarations.iterator().forEach {
            KspLog.info("${it.qualifiedName?.asString()} ${it.parentDeclaration?.qualifiedName} \n ${it.containingFile?.filePath}")
        }
        this.aapContext = aapContext
        val annotation = element.getAnnotationsByType(AapImpl::class).first()

        this.implTypeElement = element
        // it.value is KSTypeImpl
        this.apiTypeElement =
            aapContext.context.resolver.getClassDeclarationByName(
                annotation.api.qualifiedName
                    ?: throw AapException("annotation.api.qualifiedName is null")
            )
                ?: throw AapException("aapContext.context.resolver.getClassDeclarationByName(apiQualifiedName) is null")
        // 对应的接口的element对象

        this.implName = annotation.name
        this.implVersion = annotation.version

        addSuperType(element)

        if (!superTypeMap.keys.contains(annotation.api.qualifiedName)) {
            throw AapException("[${element}] not extend and interface annotation api")
        }
    }

    private fun addSuperType(element: KSClassDeclaration) {
        element.getAllSuperTypes().forEach { value ->
            value.declaration.qualifiedName?.asString()?.also { key ->
                superTypeMap[key] = value
            }
        }
    }

    override fun toString(): String {
        return """

            ----------------------------------------------------------------------------------------
            ${this.implTypeElement.simpleName}
            --->
            aapContext=            $aapContext
            --->
            apiTypeElement  = $apiTypeElement , apiQualifiedName = ${apiTypeElement.getClassCanonicalName()}
            implTypeElement = $implTypeElement , implQualifiedName = ${implTypeElement.getClassCanonicalName()}
            --->
            implName = $implName
            implVersion = $implVersion
            --->
            superTypeMap = $superTypeMap
            ----------------------------------------------------------------------------------------
        """.trimIndent()
    }

}