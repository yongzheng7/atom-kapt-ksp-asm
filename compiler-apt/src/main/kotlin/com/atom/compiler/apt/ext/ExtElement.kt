package com.atom.compiler.apt.ext

import java.lang.StringBuilder
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror


fun TypeElement.isPublic(): Boolean {
    return this.modifiers.contains(Modifier.PUBLIC)
}

fun TypeElement.isAbstract(): Boolean {
    return this.modifiers.contains(Modifier.ABSTRACT)
}

fun TypeElement.hasPublicEmptyDefaultConstructor(): Boolean {
    for (enclosed in this.enclosedElements) {
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

fun TypeElement.printString(): String {
    val result = StringBuilder()
    for (enclosed in this.enclosedElements) {
        result.append("${enclosed.simpleName}  ${enclosed.kind}  isConstructor=${enclosed.kind == ElementKind.CONSTRUCTOR} \n")
    }
    return result.toString()
}

fun Element.getTypeAnnotationMirror(
    clazz: Class<*>
): AnnotationMirror? {
    val clazzName = clazz.name
    for (m in this.annotationMirrors) {
        if (m.annotationType.toString() == clazzName) {
            return m
        }
    }
    return null
}

fun Element.getMyValue(clazz: Class<*>, key: String): TypeMirror? {
    val am = getTypeAnnotationMirror(clazz) ?: return null
    val av = am.getValue(key)
    return if (av == null) {
        null
    } else {
        av.value as TypeMirror
    }
}

fun Element.annotationToMap(clazz: Class<*>): Map<String, TypeMirror> {
    val result = mutableMapOf<String, TypeMirror>()
    val am = getTypeAnnotationMirror(clazz) ?: return result
    am.elementValues.forEach { (key, value) ->
        result[key.simpleName.toString()] = value as TypeMirror
    }
    return result
}
