package com.atom.bytecode

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.atom.module.annotation.aap.AapImpl
import com.atom.module.core.aap.AapEngine

@AapImpl(api = ActivityApi::class, name = "adsdasdad", version = 44444)
class MainActivity : AppCompatActivity(), ActivityApi {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("MainActivity", "1")
        Log.d("MainActivity", "2")
        Log.w("MainActivity", "3")
        Log.i("MainActivity", "4")
        Log.i("MainActivity", AapEngine.toString())

    }

    fun test() {
        Log.i("MainActivity", "4")
        Log.w("MainActivity", "3")
        Log.d("MainActivity", "2")
        Log.e("MainActivity", "1")
    }
}