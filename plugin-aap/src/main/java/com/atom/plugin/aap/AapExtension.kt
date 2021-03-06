package com.atom.plugin.aap

import com.atom.plugin.core.AbstractExtension

open class AapExtension : AbstractExtension() {

    val registerList = arrayListOf<SuperScanSet>()

    init {
        registerList.add(SuperScanSet("com/atom/module/annotation/aap/AapImplEntry"))
    }
}