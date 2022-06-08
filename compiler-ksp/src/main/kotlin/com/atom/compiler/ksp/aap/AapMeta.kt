package com.atom.compiler.ksp.aap

import com.atom.compiler.ksp.common.KspLog
import com.atom.compiler.ksp.ext.hasPublicEmptyDefaultConstructor
import com.atom.module.annotation.aap.AapImpl
import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import kotlin.reflect.KClass

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
    val implQualifiedName: String

    val apiTypeElement: KSClassDeclaration
    val apiQualifiedName: String

    val implName: String
    val implVersion: Long

    val superTypeMap = mutableMapOf<String, KSType>()

    constructor(aapContext: AapContext, element: KSClassDeclaration) {
        element.declarations.iterator().forEach {
            KspLog.info("${it.qualifiedName?.asString()} ${it.parentDeclaration?.qualifiedName} \n ${it.containingFile?.filePath}")
        }
        this.aapContext = aapContext
        val annotation = element.getAnnotationsByType(AapImpl::class).first()

        this.implQualifiedName = element.qualifiedName?.asString()
            ?: throw AapException("element.qualifiedName?.asString() is null")// 实现的接口的名字
        this.implTypeElement = element
        // it.value is KSTypeImpl

        this.apiQualifiedName = annotation.api.qualifiedName
            ?: throw AapException("annotation.api.qualifiedName is null")// 实现的接口的名字
        this.apiTypeElement =
            aapContext.context.resolver.getClassDeclarationByName(apiQualifiedName)
                ?: throw AapException("aapContext.context.resolver.getClassDeclarationByName(apiQualifiedName) is null")
        // 对应的接口的element对象

        this.implName = annotation.name
        this.implVersion = annotation.version

        addSuperType(element)

        if (!superTypeMap.keys.contains(this.apiQualifiedName)) {
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

    fun isApiImpl(apiQualifiedName: String): Boolean {
        return this.apiQualifiedName == apiQualifiedName
    }

    override fun toString(): String {
        return """

            ----------------------------------------------------------------------------------------
            ${this.implTypeElement.simpleName}
            --->
            aapContext=            $aapContext
            --->
            implTypeElement = $implTypeElement , implQualifiedName = $implQualifiedName
            apiTypeElement  = $apiTypeElement , apiQualifiedName = $apiQualifiedName
            --->
            implName = $implName
            implVersion = $implVersion
            --->
            superTypeMap = $superTypeMap
            ----------------------------------------------------------------------------------------
        """.trimIndent()
    }

}