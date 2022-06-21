package com.atom.bytecode

import com.atom.module.logger.LoggerIgnore

class ASMCode {

    fun test(){}

    @LoggerIgnore
    fun myCode(name: String, value: Int) {
        test()
    }

    fun asmCode(name: String, value: Int) {
        test()
    }
}