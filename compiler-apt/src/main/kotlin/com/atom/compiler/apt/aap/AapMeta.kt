package com.atom.compiler.apt.aap

import com.atom.compiler.apt.common.AptLog
import com.atom.compiler.apt.ext.hasPublicEmptyDefaultConstructor
import com.atom.compiler.apt.ext.isAbstract
import com.atom.compiler.apt.ext.isPublic
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class AapMeta(val aapContext: AapContext, val element: TypeElement) {

    companion object {
        fun create(aapContext: AapContext, element: Element, result: String): AapMeta {
            if (element !is TypeElement) {
                throw AapException("element !is TypeElement")
            }
            if (!element.isPublic()) {
                throw AapException("element is not Public")
            }
            if (element.isAbstract()) {
                throw AapException("element is Abstract")
            }
            if (!element.hasPublicEmptyDefaultConstructor()) {
                throw AapException("element has not PublicEmptyDefaultConstructor")
            }
            val result = AapMeta(aapContext, element)


            return result

        }
    }

    init {

    }
}