package com.phoenix.kspt.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import com.phoenix.kspt.models.KeyBtn
import com.phoenix.kspt.ovt.models.CustomNumber

/**
 * Callback listener for click to button iin CustomKeyboard
 */
interface PressKeyboardButton {
    /**
     * Click by keyboard's button
     * @keyBtn - button's model
     */
    fun simpleClick(keyBtn: KeyBtn, context: Context)
}

class KeyboardAdapter(context: Context, private var listener: PressKeyboardButton) : RecyclerView.Adapter<CustomButtonViewHolder>() {
    private var keyboard: ArrayList<KeyBtn> = ArrayList()
    private var inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        initKeyboard()
    }

    override fun onBindViewHolder(holder: CustomButtonViewHolder, position: Int) {
        holder.bindTo(keyboard[position])
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CustomButtonViewHolder {
        return CustomButtonViewHolder.create(inflater, viewGroup, listener)
    }

    override fun getItemCount(): Int {
        return keyboard.size
    }

    fun initKeyboard(){
        keyboard.clear()
        keyboard.add(KeyBtn("dote", ".", KeyBtn.ADD))
        keyboard.add(KeyBtn("0", "0", KeyBtn.ADD))
        keyboard.add(KeyBtn("1", "1", KeyBtn.ADD))
        keyboard.add(KeyBtn("backspace", "", KeyBtn.BACKSPACE))
        keyboard.add(KeyBtn("delete", "", KeyBtn.DELETE))

        this.notifyDataSetChanged()
    }

    fun initKeyboard(numbers: ArrayList<CustomNumber>) {
        initKeyboard()

        // additional buttons
        for (number in numbers)      // Conversation format to Summation format (0.000000.000 -> 00.000000.000)
            keyboard.add(KeyBtn(number.comment, number.numberString, KeyBtn.REINIT))

        this.notifyDataSetChanged()
    }

    companion object {
        /**
         *  Input symbols to focused EditText
         *  @keyBtn - object with data about button
         *  @numberField - focused EditText
         */
        @SuppressLint("SetTextI18n")
        fun updateTextEdit(keyBtn: KeyBtn, numberField: EditText?, context: Context): CustomNumber {
            val customNumber = CustomNumber()
            if(numberField != null) {
                val start = Math.max(numberField.selectionStart, 0)
                val end = Math.max(numberField.selectionEnd, 0)
                val length = numberField.text.toString().length

                customNumber.startPositionInEditText = start

                when (keyBtn.typeButton) {
                    KeyBtn.REINIT -> {
                        customNumber.numberString = keyBtn.data
                        customNumber.comment = keyBtn.text
                    }
                    KeyBtn.ADD -> {
                        customNumber.numberString = numberField.text.replace(Math.min(start, end),
                                Math.max(start, end), keyBtn.data, 0, keyBtn.data.length).toString()
                        customNumber.comment = ""
                    }
                    KeyBtn.BACKSPACE -> {
                        customNumber.numberString = numberField.text.replace(Math.max(0, start - 1),
                                end, "", 0, 0).toString()
                        customNumber.comment = ""
                    }
                    KeyBtn.DELETE -> {
                        customNumber.numberString = numberField.text.replace(start, Math.min(length,
                                end + 1), "", 0, 0).toString()
                        customNumber.comment = ""
                    }
                }

                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if(Build.VERSION.SDK_INT < 26)
                    oldVibrate(vibrator)
                else
                    newVibrate(vibrator)
            }

            return customNumber
        }

        /**
         * Vibration signal for android API < 26
         */
        @Suppress("DEPRECATION")
        private fun oldVibrate(vibrator: Vibrator){
            if(Build.VERSION.SDK_INT < 26)
                vibrator.vibrate(27)
        }

        /**
         * Vibration signal for android API >= 26
         */
        private fun newVibrate(vibrator: Vibrator){
            if(Build.VERSION.SDK_INT >= 26) {
                val effect = VibrationEffect.createOneShot(27, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(effect)
            }
        }
    }
}