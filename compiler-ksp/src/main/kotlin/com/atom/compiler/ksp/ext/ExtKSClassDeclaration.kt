package com.atom.compiler.ksp.ext

import com.atom.compiler.ksp.core.KspLog
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isJavaPackagePrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier

fun KSClassDeclaration.isPublic() = this.modifiers.isEmpty() || this.modifiers.containsAll(
    arrayListOf(Modifier.PUBLIC)
)

fun KSClassDeclaration.isKotlin() = extension().equals("kt", true)

fun KSClassDeclaration.isJava() = extension().equals("java", true)

fun KSClassDeclaration.extension() = this.containingFile?.fileName?.substringAfterLast('.')

fun KSClassDeclaration.hasPublicEmptyDefaultConstructor(): Boolean {
    this.getConstructors().forEach {
        if (it.isPublic() && it.parameters.isEmpty()) {
            return true
        }
    }
    return false
}

fun KSClassDeclaration.getClassCanonicalName() = this.qualifiedName?.asString()