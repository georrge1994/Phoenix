package com.phoenix.kspt.profile

import android.annotation.SuppressLint
import android.support.v4.content.ContextCompat.getColor
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.phoenix.kspt.R
import kotlinx.android.synthetic.main.item_group.view.*

/**
 * View holder for recycler in profile fragment (groups for professor, courses for students)
 */
class RecyclerViewHolder private constructor(view: View,
                                             val listener: SimpleRecyclerViewClickListener) : RecyclerView.ViewHolder(view) {

    private val itemGroup = view.itemGroup

    @SuppressLint("SetTextI18n")
    fun bindTo(name: String) {
        // reset form

        if (selectedPosition == layoutPosition) {
            itemGroup.setBackgroundColor(getColor(itemGroup.context, R.color.blue))
            selectedPosition = -1
        } else
            itemGroup.setBackgroundColor(getColor(itemGroup.context, R.color.white))

        // set data
        itemGroup.name.text = name
        itemGroup.number.text = (1 + adapterPosition).toString() + "."
        // set listeners
        itemGroup.setOnClickListener { selectAndOpen(name) }
        itemGroup.moreBtn.setOnClickListener { selectAndOpen(name) }
    }

    private fun selectAndOpen(name: String) {
        listener.openIt(name)
        selectedPosition = layoutPosition
    }

    companion object {
        var selectedPosition = -1
        fun create(inflater: LayoutInflater, parent: ViewGroup, listener: SimpleRecyclerViewClickListener): RecyclerViewHolder {
            return RecyclerViewHolder(
                    inflater.inflate(R.layout.item_group, parent, false),
                    listener)
        }
    }
}