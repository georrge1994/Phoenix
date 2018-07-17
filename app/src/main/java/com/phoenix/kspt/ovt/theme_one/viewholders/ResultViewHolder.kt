package com.phoenix.kspt.ovt.theme_one.viewholders

import android.annotation.SuppressLint
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.phoenix.kspt.R
import com.phoenix.kspt.ovt.models.Stage
import kotlinx.android.synthetic.main.ovt_1_item_result.view.*

class ResultViewHolder private constructor(view: View) : RecyclerView.ViewHolder(view) {
    private val itemResult = view.itemResult

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup): ResultViewHolder {
            return ResultViewHolder(inflater.inflate(R.layout.ovt_1_item_result, parent, false))
        }
    }

    @SuppressLint("SetTextI18n")
    fun bindTo(stage: Stage) {
        itemResult.name.text = stage.name
        itemResult.countAttempt.text = stage.countAttempts.toString()
        if (stage.stageCompleted) {
            itemResult.mark.setTextColor(ContextCompat.getColor(itemResult.context, R.color.green))
            itemResult.mark.text = itemResult.context.getString(R.string.result_ok)
        } else {
            itemResult.mark.setTextColor(ContextCompat.getColor(itemResult.context, R.color.red))
            itemResult.mark.text = itemResult.context.getString(R.string.result_bad)
        }
    }
}