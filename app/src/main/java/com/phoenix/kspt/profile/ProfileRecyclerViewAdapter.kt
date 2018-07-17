package com.phoenix.kspt.profile

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Callback listener for open group(for professor) and subject(for students)
 */
interface SimpleRecyclerViewClickListener {
    /**
     * Just open it
     */
    fun openIt(groupName: String)
}

class ProfileRecyclerViewAdapter(context: Context, private var items: ArrayList<String>,
                                 private val listener: SimpleRecyclerViewClickListener) :
        RecyclerView.Adapter<RecyclerViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.bindTo(items[position])
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerViewHolder {
        return RecyclerViewHolder.create(inflater, viewGroup, listener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addAll(items: ArrayList<String>) {
        this.items.addAll(items)
        notifyDataSetChanged()
    }
}
