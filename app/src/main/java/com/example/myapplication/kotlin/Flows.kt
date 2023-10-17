package com.example.myapplication.kotlin

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.minutes

fun main(vararg args: String) {
    println(getCurrentTime())

    val flow = flow<String> {
        repeat(5) {
            emit("Hello World")
            delay(1000)
        }
        emit("Hello World")
        emit("Hello World")
        emit("Hello World")
        emit("Hello World")
    }

    GlobalScope.launch {
        flow.collect {
            println(it + " " + getCurrentTime())
            delay(2000)
        }
    }




    runBlocking {
        delay(100_000)
    }
}

fun getCurrentTime(): String {
    return "${Date(System.currentTimeMillis()).minutes}:${Date(System.currentTimeMillis()).seconds}"
}