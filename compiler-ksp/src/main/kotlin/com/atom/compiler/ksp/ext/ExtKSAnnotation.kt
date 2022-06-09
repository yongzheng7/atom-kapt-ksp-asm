package com.atom.compiler.ksp.ext

import com.google.devtools.ksp.symbol.KSAnnotation

fun KSAnnotation.getValue(
    key: String
): Any? {
    for (bean in this.arguments) {
        if(bean.name?.asString() == key)
            return bean.value
    }
    return null
}