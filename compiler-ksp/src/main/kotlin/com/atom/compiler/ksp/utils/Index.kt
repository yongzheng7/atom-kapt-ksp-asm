package com.atom.compiler.ksp.utils

import com.atom.compiler.ksp.core.KspLog
import com.atom.module.annotation.aap.AapImpl
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType

class Index(resolver: Resolver) {

    companion object {
        val instance: Index
            get() = currentInstance!!

        private var currentInstance: Index? = null

        operator fun contains(declaration: KSDeclaration): Boolean {
            return declaration in instance.typesFromCurrentIndex ||
                    declaration in instance.typesFromLibraryIndex
        }

        fun release() {
            currentInstance = null
        }
    }

    init {
        currentInstance = this
    }

    val typesFromLibraryIndex by lazy {
        resolver.getDeclarationsFromPackage(IndexGenerator.INDEX_PACKAGE)
            .filterIsInstance<KSClassDeclaration>()
            .flatMap {
                it.getAnnotationsByType(AapImpl::class)
            }.flatMap {
                it.name.asSequence()
            }.mapNotNull {
                resolver.getClassDeclarationByName(resolver.getKSNameFromString(it.toString()))
            }.toSet()
            .onEach {
                KspLog.info(">>> ${it.qualifiedName!!.asString()}")
            }
    }

    val typesFromCurrentIndex by lazy {
        currentConfigs.flatMap {
            it.annotations
        }.flatMap {
            it.arguments
        }.flatMap {
            when (val value = it.value) {
                is List<*> -> value.asSequence()
                else -> sequenceOf(value)
            }
        }.filterIsInstance<KSType>()
            .map { it.declaration }
            .filterIsInstance<KSClassDeclaration>()
            .toSet()
    }

    // 获取被AapImpl注解的class集合
    val currentConfigs by lazy {
        resolver.getSymbolsWithAnnotation(AapImpl::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
    }

    fun generateCurrent() {
        IndexGenerator().generate(
            typesFromCurrentIndex, currentConfigs.mapNotNull { it.containingFile }.toList()
        )
    }

}