package com.atom.compiler.apt.aap

class AapMetas {

    val aapContext: AapContext
    val aapPacket: String = AapOptions.AAP_PACKET
    val aapModuleName: String

    constructor(aapContext: AapContext) {
        this.aapContext = aapContext
        this.aapModuleName = aapContext.moduleName
    }

    fun writeFile(mates: Set<AapMeta>) {
        // 生产指定名称的class类,可以继承或者实现某个或者某些接口
    }
}