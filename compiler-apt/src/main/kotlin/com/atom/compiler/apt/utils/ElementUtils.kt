package com.atom.compiler.apt.utils

import com.atom.compiler.apt.common.AptContext
import com.atom.compiler.apt.common.AptLog
import com.atom.compiler.apt.common.AptLog.error
import java.util.*
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.TypeVariable

object ElementUtils {


    const val DEFAULT_ANNOTATION_PARAMETER_NAME = "value"
    private val PRIMITIVES = mutableMapOf<String, String>()

    init {
        PRIMITIVES["char"] = "Character"

        PRIMITIVES["byte"] = "Byte"
        PRIMITIVES["short"] = "Short"
        PRIMITIVES["int"] = "Integer"
        PRIMITIVES["long"] = "Long"

        PRIMITIVES["boolean"] = "Boolean"

        PRIMITIVES["float"] = "Float"
        PRIMITIVES["double"] = "Double"
    }

    fun toTypeString(type: TypeMirror): String {
        //获取全类名
        return if (type.kind.isPrimitive) {
            PRIMITIVES[type.toString()]!!
        } else type.toString()
    }

    fun getSuperclassTypeElement(element: TypeElement): TypeElement? {
        val superClass = element.superclass
        return if (superClass.kind == TypeKind.DECLARED) {
            val superClassElement = (superClass as DeclaredType).asElement()
            superClassElement as TypeElement
        } else {
            null
        }
    }

    fun extractClosestRealTypeAsString(type: TypeMirror, context: AptContext): String {
        return if (type is TypeVariable) {
            val compositeUpperBound = type.upperBound
            extractClosestRealTypeAsString(compositeUpperBound, context)
        } else {
            context.types.erasure(type).toString()
        }
    }

    fun containsAnnotation(element: Element, vararg annotations: String): Boolean {
        val annotationClassNames = mutableListOf<String>()
        Collections.addAll(annotationClassNames, *annotations)
        for (mirror in element.annotationMirrors) {
            if (annotationClassNames.contains(mirror.annotationType.toString())) {
                return true
            }
        }
        return false
    }

    fun isAnnotationMirrorOfType(annotationMirror: AnnotationMirror, fqcn: String): Boolean {
        return annotationMirror.annotationType.toString() == fqcn
    }

    fun getAnnotationMirror(element: Element, fqcn: String): AnnotationMirror? {
        var mirror: AnnotationMirror? = null
        for (am in element.annotationMirrors) {
            if (isAnnotationMirrorOfType(am, fqcn)) {
                mirror = am
                break
            }
        }
        return mirror
    }

    fun getAnnotationValue(annotationMirror: AnnotationMirror, parameterValue: String): Any? {
        var returnValue: Any? = null
        for ((key, value) in annotationMirror.elementValues) {
            if (parameterValue == key.simpleName.toString()) {
                returnValue = value.value
                break
            }
        }
        return returnValue
    }

    fun getCollectionElementType(
        t: DeclaredType,
        fqNameOfReturnedType: String,
        explicitTargetEntityName: String?,
        context: AptContext
    ): TypeMirror? {
        val collectionElementType: TypeMirror
        collectionElementType = if (explicitTargetEntityName != null) {
            val elements = context.elements
            val element = elements.getTypeElement(explicitTargetEntityName)
            element.asType()
        } else {
            val typeArguments = t.typeArguments
            if (typeArguments.size == 0) {
                throw RuntimeException("\n Unable to determine collection type")
            } else if (MutableMap::class.java.canonicalName == fqNameOfReturnedType) {
                t.typeArguments[1]
            } else {
                t.typeArguments[0]
            }
        }
        return collectionElementType
    }

    fun getKeyType(t: DeclaredType, context: AptContext): String {
        val typeArguments = t.typeArguments
        if (typeArguments.size == 0) {
            error("\n Unable to determine type argument for $t")
        }
        return extractClosestRealTypeAsString(typeArguments[0], context)
    }

    fun hasPublicEmptyDefaultConstructor(classElement: TypeElement): Boolean {
        for (enclosed in classElement.enclosedElements) {
            if (enclosed.kind == ElementKind.CONSTRUCTOR) {
                val constructorElement = enclosed as ExecutableElement
                if (constructorElement.parameters.size == 0
                    && constructorElement.modifiers.contains(Modifier.PUBLIC)
                ) {
                    return true
                }
            }
        }
        return false
    }

    fun isAssignableInterfaceClass(
        context: AptContext,
        classElement: TypeElement,
        interfaceClassTypeMirror: TypeMirror
    ): Boolean {
        var currElement = classElement
        if (currElement.qualifiedName.toString() == interfaceClassTypeMirror.toString()) {
            return true
        }
        while (true) {
            val interfaces = currElement.interfaces
            AptLog.info("The class ${currElement.qualifiedName}")
            if (interfaces.contains(interfaceClassTypeMirror)) {
                return true
            }
            for (typeMirror in interfaces) {
                if (isAssignableInterfaceClass(
                        context,
                        context.types.asElement(typeMirror) as TypeElement,
                        interfaceClassTypeMirror
                    )
                ) {
                    return true
                }
            }
            val superClassType = currElement.superclass
            if (superClassType.kind == TypeKind.NONE) {
                return false
            }
            currElement = context.types.asElement(superClassType) as TypeElement
        }
    }

    fun isAssignableSuperClass(
        context: AptContext,
        classElement: TypeElement,
        superClassElement: TypeMirror
    ): Boolean {
        var currentClass = classElement
        AptLog.info("The class ${classElement.qualifiedName}")
        while (true) {
            val superClassType = currentClass.superclass
            AptLog.info("\n The class $superClassType")
            if (superClassType.kind == TypeKind.NONE) {
                return false
            }
            if (superClassType == superClassElement) {
                return true
            }
            currentClass = context.types.asElement(superClassType) as TypeElement
        }
    }

    fun isAssignable(
        context: AptContext,
        classElement: TypeElement,
        superClassElement: TypeElement
    ): Boolean {
        val currClassQualifiedName = classElement.qualifiedName.toString()
        val superClassQualifiedName = superClassElement.qualifiedName.toString()
        AptLog.info("\n isAssignable curr = $currClassQualifiedName   super = $superClassQualifiedName")
        if (currClassQualifiedName == superClassQualifiedName) {
            return true
        }
        return if (ElementKind.INTERFACE == superClassElement.kind) {
            isAssignableInterfaceClass(context, classElement, superClassElement.asType())
        } else {
            isAssignableSuperClass(context , classElement, superClassElement.asType())
        }
    }
}