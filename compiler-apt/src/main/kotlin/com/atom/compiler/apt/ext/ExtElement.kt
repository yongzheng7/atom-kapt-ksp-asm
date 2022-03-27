package com.atom.compiler.apt.ext

import java.lang.StringBuilder
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement


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