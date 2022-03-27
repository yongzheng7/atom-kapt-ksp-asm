package com.atom.module.api.aap

import androidx.annotation.NonNull

interface AapContextAware {

    fun setAapContext(@NonNull aapContext: AapContext)
}