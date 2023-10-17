package com.example.myapplication.kotlin

import kotlinx.coroutines.*

fun main(vararg args: String) {

    val job = GlobalScope.launch {
        repeat(40) {
            if (isActive)
                println("result for $it is ${fib(it)}")
        }
    }

    runBlocking {
        job.cancel()
        println("*******************************************")
        delay(20_000L)

    }

}


fun fibs(i: Int): Long {
    return if (i == 0) 0
    else if (i == 1) 1
    else fibs(i-1) + fibs(i -2)
}
suspend fun fib(i: Int): Long {
    return if (i == 0) 0
    else if (i == 1) 1
    else fib(i-1) + fib(i -2)
}