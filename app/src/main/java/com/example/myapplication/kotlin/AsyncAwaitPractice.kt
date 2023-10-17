package com.example.myapplication.kotlin

import kotlinx.coroutines.*

fun main(vararg args: String) {

//    val job1 = GlobalScope.async {
//        delay(1000)
//        println("from job 1 ${fib(37)}")
//        "job 1"
//    }
//    val job2 = GlobalScope.launch {
//        delay(2000)
//        println("from job 2 ${fib(37)}")
//        "job 2"
//    }
//    val job3 = GlobalScope.launch {
//        delay(3000)
//        println("from job 3 ${fib(37)}")
//        "job 3"
//    }
//    println("end of jobs definition")
//    runBlocking {
//
//        println(job1.await())
//
//        println("after join")
//        delay(5_000L)
//    }


    var ans1: String? = null
    val ans1Job = GlobalScope.launch { ans1 = answer1() }
    GlobalScope.launch {
        ans1Job.join()//if we comment this print will be null because coroutine pass and not wait until job finish job
        println(ans1)
    }

//            ||

    val ans2Job = GlobalScope.async { answer1() }
    GlobalScope.launch { println(ans2Job.await()) }



    runBlocking { delay(10_000) }
}

suspend fun answer1(): String {
    delay(1000)
    return "answer 1"
}
