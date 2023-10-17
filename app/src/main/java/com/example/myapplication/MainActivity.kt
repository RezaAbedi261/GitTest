package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch {
            while (true) {
                delay(1000)
                println("Global scope running")
            }
        }
        lifecycleScope.launch {
            while (true) {
                delay(1000)
                println("lifecycleScope scope running")
            }
        }

        lifecycleScope.launch {
            delay(5000)
            startActivity(Intent(this@MainActivity, MainActivity2::class.java))
            finish()
        }



    }



}