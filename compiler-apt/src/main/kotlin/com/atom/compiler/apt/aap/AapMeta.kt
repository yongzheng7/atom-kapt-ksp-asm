package com.atom.compiler.apt.aap

import com.atom.compiler.apt.ext.*
import com.atom.module.annotation.aap.AapImpl
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

class AapMeta {

    companion object {
        fun create(aapContext: AapContext, element: Element): AapMeta {
            if (element !is TypeElement) {
                throw AapException("${element.simpleName} !is TypeElement")
            }
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
    val implTypeElement: TypeElement
    val implQualifiedName: String
    val apiTypeElement: TypeElement
    val apiQualifiedName: String

    val implName: String
    val implVersion: Long

    val superTypeMap = mutableMapOf<String, TypeElement>()
    val interfaceTypeMap = mutableMapOf<String, TypeElement>()

    constructor(aapContext: AapContext, element: TypeElement) {

        val annotation = element.getAnnotation(AapImpl::class.java)
        this.aapContext = aapContext

        this.implQualifiedName = element.qualifiedName.toString()
        this.implTypeElement = element

        this.apiQualifiedName = element.getMyValue(AapImpl::class.java, "api").toString()// 实现的接口的名字
        this.apiTypeElement =
            aapContext.context.elements.getTypeElement(this.apiQualifiedName) // 对应的接口的element对象

        this.implName = annotation.name
        this.implVersion = annotation.version
        addInterface(element)
        addSuper(element)
        if (!(superTypeMap.keys.contains(this.apiQualifiedName)
                    || interfaceTypeMap.keys.contains(this.apiQualifiedName))
        ) {
            throw AapException("[${element}] not extend and interface annotation api")
        }
    }

    private fun addSuper(element: TypeElement) {
        element.superclass.run {
            if (this is DeclaredType) {
                val superClassTypeElement: TypeElement = this.asElement() as TypeElement
                superTypeMap[superClassTypeElement.qualifiedName.toString()] = superClassTypeElement
                addSuper(superClassTypeElement)
            }
        }
    }

    private fun addInterface(element: TypeElement) {
        for (interfaceEntry in element.interfaces) {
            //  检查 实现或者继承的接口和父类 是否和api能够对应
            if (interfaceEntry is DeclaredType) {
                val superClassTypeElement: TypeElement = interfaceEntry.asElement() as TypeElement
                interfaceTypeMap[superClassTypeElement.qualifiedName.toString()] =
                    superClassTypeElement
                addInterface(superClassTypeElement)
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
            --->
            interfaceTypeMap = $interfaceTypeMap
            ----------------------------------------------------------------------------------------
        """.trimIndent()
    }

}