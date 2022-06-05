package com.atom.compiler.apt.aap

import com.atom.compiler.apt.common.AptLog
import com.atom.compiler.apt.ext.hasPublicEmptyDefaultConstructor
import com.atom.compiler.apt.ext.isAbstract
import com.atom.compiler.apt.ext.isPublic
import com.atom.compiler.apt.utils.ElementUtils
import com.atom.module.annotation.aap.AapImpl
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
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
            val result = AapMeta(aapContext, element)
            if (Object::class.qualifiedName.equals(result.apiQualifiedName)) {
                return result
            }
            val apiTypeElement = result.apiTypeElement
            if (!ElementUtils.isAssignable(aapContext.context, element, apiTypeElement)) {
                val superClassName: String = apiTypeElement.qualifiedName.toString()
                throw AapException(
                    "The class ${element.qualifiedName}" +
                            " annotated with @${AapImpl::class.java.simpleName}" +
                            if (ElementKind.INTERFACE == apiTypeElement.kind)
                                " must implement the interface $superClassName" else
                                " must inherit from  $superClassName"
                )
            }
            return result
        }
    }

    val aapContext: AapContext
    val implTypeElement: TypeElement
    val implQualifiedName: String
    val apiTypeElement: TypeElement
    val apiQualifiedName: String

    val annotationApi: String
    val annotationName: String
    val annotationVersion: Long

    val superTypeMap = mutableMapOf<String, TypeElement>()
    val interfaceTypeMap = mutableMapOf<String, TypeElement>()

    constructor(aapContext: AapContext, element: TypeElement) {
        val annotation: AapImpl = element.getAnnotation(AapImpl::class.java)

        this.aapContext = aapContext

        this.implQualifiedName = element.qualifiedName.toString()
        this.implTypeElement = element

        this.apiQualifiedName = annotation.api // 实现的接口的名字
        this.apiTypeElement =
            aapContext.context.elements.getTypeElement(annotation.api) // 对应的接口的element对象

        this.annotationApi = annotation.api
        this.annotationName = annotation.name
        this.annotationVersion = annotation.version
        // TODO 找到接口
        //addInterface(element)
        // TODO 父类
        //addSuper(element)
        // TODO 进行检查是否继承和实现 注解中api的
//        if (!(superTypeMap.keys.contains(this.apiQualifiedName)
//                    || interfaceTypeMap.keys.contains(this.apiQualifiedName))
//        ) {
//            throw AapException("[${element}] not extend and interface annotation api")
//        }
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
            annotationApi = $annotationApi
            annotationName = $annotationName
            annotationVersion = $annotationVersion
            --->
            superTypeMap = $superTypeMap
            --->
            interfaceTypeMap = $interfaceTypeMap
            ----------------------------------------------------------------------------------------
        """.trimIndent()
    }

}