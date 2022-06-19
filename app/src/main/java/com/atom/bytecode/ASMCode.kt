package com.atom.bytecode

import com.atom.module.logger.Logger

class ASMCode {

    fun test(){}

    fun myCode(name: String, value: Int) {
        Logger.i("ASMCode" , "myCode" , "start")
        test()
        Logger.e("ASMCode" , "myCode" , "end")
    }

    fun asmCode(name: String, value: Int) {

    }
}