package com.atom.compiler.apt.ext

import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue

fun AnnotationMirror.getValue(
    key: String
): AnnotationValue? {
    for ((key1, value) in this.elementValues) {
        if (key1!!.simpleName.toString() == key) {
            return value
        }
    }
    return null
}