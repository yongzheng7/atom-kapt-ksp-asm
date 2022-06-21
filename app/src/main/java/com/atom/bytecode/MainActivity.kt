package com.atom.bytecode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.atom.module.annotation.aap.AapImpl
import com.atom.module.core.aap.AapEngine
import com.atom.module.logger.Logger

@AapImpl(api = ActivityApi::class, name = "adsdasdad", version = 44444)
class MainActivity : AppCompatActivity(), ActivityApi {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ASMCode().also {
            it.myCode("asd", 1)
            it.asmCode("asd", 1)
        }
    }
}