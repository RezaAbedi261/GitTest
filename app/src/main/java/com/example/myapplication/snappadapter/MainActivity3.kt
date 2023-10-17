package com.example.myapplication.snappadapter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.OfferAdapter
import com.example.myapplication.R
import com.example.myapplication.snappadapter.layoutmanager.LoopingLayoutManager

class MainActivity3 : AppCompatActivity() {
    var recyclerView: RecyclerView? = null
    var offerAdapter: OfferAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerVieww)

        recyclerView?.layoutManager = LoopingLayoutManager(
            applicationContext,
            LoopingLayoutManager.HORIZONTAL,
            false,
            LoopingLayoutManager.TOWARDS_HIGHER_INDICES
        )

        offerAdapter = OfferAdapter(

        )
        recyclerView?.adapter = offerAdapter

    }
}