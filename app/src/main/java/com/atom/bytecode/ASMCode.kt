package com.atom.bytecode

import com.atom.bytecode.annotation.HookIgnore

class ASMCode {

    fun test(){}

    @HookIgnore
    fun myCode(name: String, value: Int) {
        test()
    }

    fun asmCode(name: String, value: Int) {
        test()
    }
}