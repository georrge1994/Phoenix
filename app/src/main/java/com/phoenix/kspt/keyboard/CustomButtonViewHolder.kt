package com.phoenix.kspt.keyboard

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.phoenix.kspt.R
import com.phoenix.kspt.models.KeyBtn
import kotlinx.android.synthetic.main.item_keyboard.view.*

/**
 * Viewholder for button in the custom keyboard
 */
class CustomButtonViewHolder private constructor(private val view: View,
                                                 private val listener: PressKeyboardButton) :
        RecyclerView.ViewHolder(view), View.OnTouchListener {
    val itemKeyboard = view.itemKeyboard!!

    companion object {
        fun create(inflater: LayoutInflater,
                   parent: ViewGroup,
                   listener: PressKeyboardButton
        ): CustomButtonViewHolder {
            return CustomButtonViewHolder(
                    inflater.inflate(R.layout.item_keyboard, parent, false),
                    listener)
        }
    }

    @SuppressLint("SetTextI18n")
    fun bindTo(keyBtn: KeyBtn) {
        itemKeyboard.text = keyBtn.text
        view.setOnClickListener { listener.simpleClick(keyBtn, view.context) }
        view.setOnTouchListener(this)
    }

    /**
     * Listener of the touching. It is help to change button's color
     */
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> itemKeyboard.isSelected = true
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> itemKeyboard.isSelected = false
        }

        return false
    }
}