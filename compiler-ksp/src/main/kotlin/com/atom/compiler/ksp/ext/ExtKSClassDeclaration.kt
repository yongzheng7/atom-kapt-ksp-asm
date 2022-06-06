package com.atom.compiler.ksp.ext

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier

fun KSClassDeclaration.hasPublicEmptyDefaultConstructor(): Boolean {
    this.getConstructors().forEach {
        if (it.modifiers.contains(Modifier.PUBLIC) && it.parameters.isEmpty()) {
            return true
        }
    }
    return false
}