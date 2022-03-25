package com.atom.bytecode

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.atom.module.annotation.aap.AapImpl

@AapImpl(api = Activity::class)
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("MainActivity", "1")
        Log.d("MainActivity", "2")
        Log.w("MainActivity", "3")
        Log.i("MainActivity", "4")
    }
}