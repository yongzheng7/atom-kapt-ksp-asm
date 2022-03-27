package com.atom.module.api.aap

import com.atom.module.annotation.aap.AapImplVersion

interface AapFilter<T> {
    fun accept(clazz: Class<out T>, param: AapImplVersion)
}