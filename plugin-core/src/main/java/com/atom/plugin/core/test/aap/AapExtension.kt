package com.atom.plugin.core.test.aap

import com.atom.plugin.core.AbstractExtension

open class AapExtension : AbstractExtension() {

    val registerList = arrayListOf<SuperScanSet>()

    init {
        registerList.add(SuperScanSet("com/atom/annotation/bean/ApiImpls"))
    }
}