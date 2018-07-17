package com.phoenix.kspt.ovt.theme_one.viewholders

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.phoenix.kspt.R
import com.phoenix.kspt.ovt.models.CustomNumber
import com.phoenix.kspt.ovt.theme_one.adapters.NumberFieldListener
import kotlinx.android.synthetic.main.ovt_1_item_conv_number.view.*

class ConvBinaryViewHolder private constructor(view: View,
                                               listener: NumberFieldListener,
                                               launchMode: String) : ParentNumberViewHolder(view, listener, launchMode) {
    init {
        itemNumber = view.itemNumber!!
    }

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup, listener: NumberFieldListener,
                   launchMode: String): ConvBinaryViewHolder {
            return ConvBinaryViewHolder(inflater.inflate(R.layout.ovt_1_item_conv_number,
                    parent, false), listener, launchMode)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindTo(customNumber: CustomNumber, invalidatePosition: Int) {
        super.bindTo(customNumber, invalidatePosition)
        itemNumber.number.showSoftInputOnFocus = false
    }
}