package com.phoenix.kspt.ovt.theme_one.viewholders

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.phoenix.kspt.ovt.models.CustomNumber
import com.phoenix.kspt.R
import com.phoenix.kspt.ovt.theme_one.adapters.NumberFieldListener
import kotlinx.android.synthetic.main.ovt_1_item_sum_binary.view.*

open class SumDecimalViewHolder internal constructor(view: View,
                                                     listener: NumberFieldListener,
                                                     launchMode: String) : ParentNumberViewHolder(view, listener, launchMode) {
    init {
        itemNumber = view.itemNumber!!
    }

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup, listener: NumberFieldListener,
                   launchMode: String): SumDecimalViewHolder {
            return SumDecimalViewHolder(inflater.inflate(R.layout.ovt_1_item_sum_decimal, parent, false),
                    listener, launchMode)
        }
    }

    @SuppressLint("SetTextI18n")
    open fun bindTo(customNumber: CustomNumber, isLast: Boolean, invalidatePosition: Int) {
        super.bindTo(customNumber, invalidatePosition)

        // Init visibly elements
        when {
            customNumber.isPlusOperation() && adapterPosition % 2 == 0 -> {
                if (isLast)
                    itemNumber.sign.text = "="
                else
                    itemNumber.sign.text = "+"

                itemNumber.sign.visibility = View.VISIBLE
                itemNumber.divider.visibility = View.GONE
            }
            customNumber.isTransformOperation() -> {
                itemNumber.sign.text = "="
                itemNumber.sign.visibility = View.VISIBLE
                itemNumber.divider.visibility = View.GONE
            }
            else -> {
                itemNumber.sign.visibility = View.GONE
                itemNumber.divider.visibility = View.VISIBLE
            }
        }
    }
}