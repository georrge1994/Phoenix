package com.phoenix.kspt.ovt.theme_one.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.phoenix.kspt.ovt.models.Stage
import com.phoenix.kspt.ovt.theme_one.viewholders.ResultViewHolder

class ResultsAdapter(context: Context, private var items: ArrayList<Stage>) :
        RecyclerView.Adapter<ResultViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)


    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bindTo(items[position])
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ResultViewHolder {
        return ResultViewHolder.create(inflater, viewGroup)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem(stage: Stage, name: String) {
        stage.name = name
        items.add(stage)
        this.notifyDataSetChanged()
    }
}