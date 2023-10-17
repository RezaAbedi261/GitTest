package com.example.myapplication.snappadapter

data class FakeData(
    val color: Int
) {
    @Transient var nextOffer: FakeData? = null
    @Transient var previousOffer: FakeData? = null
}