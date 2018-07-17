package com.phoenix.kspt.ovt.theme_one.viewholders

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.phoenix.kspt.R
import com.phoenix.kspt.ovt.models.CustomNumber
import com.phoenix.kspt.ovt.theme_one.BinaryFunctions
import com.phoenix.kspt.ovt.theme_one.adapters.NumberFieldListener
import com.phoenix.kspt.utils.PROFESSOR_MODE
import kotlinx.android.synthetic.main.ovt_1_item_conv_number.view.*

open class ParentNumberViewHolder internal constructor(val view: View,
                                                       val listener: NumberFieldListener,
                                                       val launchMode: String) : RecyclerView.ViewHolder(view){
    protected var itemNumber = view.itemNumber!!

    companion object {
        fun create(inflater: LayoutInflater, parent: ViewGroup, listener: NumberFieldListener,
                   launchMode: String): ParentNumberViewHolder {
            return ParentNumberViewHolder(inflater.inflate(R.layout.ovt_1_item_conv_number,
                    parent, false), listener, launchMode)
        }
    }

    @SuppressLint("SetTextI18n")
    open fun bindTo(customNumber: CustomNumber, invalidatePosition: Int) {

        // Init data
        when {
            customNumber.numberString.isNotEmpty() -> itemNumber.number.setText(customNumber.numberString)
            BinaryFunctions.getNumberInString(customNumber).isNotEmpty() -> itemNumber.number.setText(BinaryFunctions.getNumberInString(customNumber))
            else -> itemNumber.number.text.clear()
        }

        when {
            customNumber.comment.isNotEmpty() -> itemNumber.comment.text = "// ${customNumber.comment}"
            else -> itemNumber.comment.text = ""
        }

        itemNumber.number.error = null
        if (invalidatePosition == adapterPosition)
            itemNumber.number.error = listener.notification

        // set focus after notify data set changed
        itemNumber.number.setSelection(Math.max(0, customNumber.startPositionInEditText - 1))

        // listeners
        itemNumber.number.setOnFocusChangeListener { _, hasFocus ->
            if(hasFocus)
                listener.onFocus(hasFocus, adapterPosition)
            else
                listener.onFocus(hasFocus, -1)
        }

        // ChangeBottomListeners
        val typeViewHolder = this.itemViewType
        itemNumber.number.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // set cursor for adding char
                if(typeViewHolder == 0)
                    itemNumber.number.setSelection(start)       // binary code in summation (set in the end of EditText)
                                                                // binary code in conversation,
                                                                // decimal code in summation (set in the end of EditText)
                // update item
                listener.updateItem(itemNumber.number.text.toString(), adapterPosition)
            }

            override fun afterTextChanged(s: Editable) { }
        })
    }
}