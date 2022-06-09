package com.atom.compiler.ksp.ext

import com.atom.compiler.ksp.core.KspLog
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror
import kotlin.reflect.KClass


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


fun KSClassDeclaration.getAnnotation(
    clazz: KClass<*>
): KSAnnotation? {
    val clazzName = clazz.simpleName
    for (m in this.annotations) {
        if (m.shortName.asString() == clazzName) {
            return m
        }
    }
    return null
}

fun KSClassDeclaration.getAnnotationValue(clazz: KClass<*>, key: String): Any? {
    val am = getAnnotation(clazz) ?: return null
    val av = am.getValue(key) ?: return null
    return if (av is KSType) {
        av.declaration.qualifiedName?.asString()
    } else {
        av
    }
}

fun KSClassDeclaration.annotationToMap(clazz: KClass<*>): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>()
    val am = getAnnotation(clazz) ?: return result
    for (argument in am.arguments) {
        result[argument.name?.asString() ?: continue] = argument.value.let {
            if(it is KSType){
                it.declaration.qualifiedName?.asString()
            }else{
                it
            }
        }
    }
    return result
}
