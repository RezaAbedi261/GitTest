package com.example.myapplication.snappadapter.layoutmanager

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.snappadapter.FakeData

/**
 * This class is responsible to handle showing all features related to an offer event and also handling the ui events
 * including those which are required to pass to upper layer(Adapter) or are required to be consumed internally
 */
class TestViewHolder(
    /**
     * the item view of the [RecyclerView.ViewHolder]
     */
    view: View,

) : RecyclerView.ViewHolder(view) {


    fun bind(offer: FakeData?) {

    }

}