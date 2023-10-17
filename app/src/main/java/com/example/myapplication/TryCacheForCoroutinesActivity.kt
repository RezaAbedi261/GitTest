package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

class TryCacheForCoroutinesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_try_cache_for_coroutines)

//        try { // lead to crash
//            lifecycleScope.launch {
//                throw Exception()
//            }//.await()
//        }catch (e: Exception) {
//            e.printStackTrace()
//        }


//        try {// lead to crash
//            lifecycleScope.launch {
//                throw Exception()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }

//        val deferred = lifecycleScope.async { // not crash but not a good pattern
//            throw Exception("asd")
//        }
//        lifecycleScope.launch { // lead to crash
//            deferred.await()
//        }
//        lifecycleScope.async { // not crash but not a good pattern
//            deferred.await()
//        }

//        lifecycleScope.launch { // not crash but not good pattern
//            try {
//                deferred.await()
//            }catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }


//        val exceptionHandler1 = CoroutineExceptionHandler { coroutineContext, throwable -> }
//        lifecycleScope.launch(exceptionHandler1) {
//            throw Exception("")
//        }
//        val exceptionHandler2 = CoroutineExceptionHandler { coroutineContext, throwable -> }
//        CoroutineScope(Dispatchers.Main + exceptionHandler2).launch(exceptionHandler2) {
//            launch {
//                delay(1000)
//                println("launch 1")
//                throw Exception("")
//            }
//            launch {
//                delay(1000)
//                println("launch 2")
//            }
//        }


       lifecycleScope.launch {
           val job = launch {
               try {
                   delay(500)
               }catch (e:Exception) {
                   if (e is CancellationException)
                       throw e
                   println("coroutine exception ${e.message}")
               }
               println("coroutine finished")
           }
           delay(300)
           job.cancel()
       }


    }
}