package com.phoenix.kspt.ovt.theme_one.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.phoenix.kspt.keyboard.KeyboardAdapter
import com.phoenix.kspt.models.KeyBtn
import com.phoenix.kspt.models.KeyBtn.CREATOR.REINIT
import com.phoenix.kspt.ovt.models.CustomNumber
import com.phoenix.kspt.ovt.theme_one.viewholders.ConvBinaryViewHolder
import com.phoenix.kspt.ovt.theme_one.viewholders.ParentNumberViewHolder
import com.phoenix.kspt.ovt.theme_one.viewholders.SumBinaryViewHolder
import com.phoenix.kspt.ovt.theme_one.viewholders.SumDecimalViewHolder
import com.phoenix.kspt.utils.CONVERSATION
import com.phoenix.kspt.utils.FROM_DIRECT_TO_DECIMAL
import com.phoenix.kspt.utils.PLUS
import com.phoenix.kspt.utils.TRANSFORM
import kotlinx.android.synthetic.main.ovt_1_item_conv_number.view.*

class RecyclerViewNumbersAdapter(context: Context,
                                 private var items: ArrayList<CustomNumber>,
                                 private var listener: NumberFieldListener,
                                 private var launchMode: String,
                                 private var layoutManager: RecyclerView.LayoutManager) :
        RecyclerView.Adapter<ParentNumberViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var invalidatePosition: Int = -1
    private var focusedPosition: Int = -1
    private var viewType = 2

    override fun getItemViewType(position: Int): Int {
        return when(items[position].typeOperation) {
            PLUS -> 0   // sum fragment
            FROM_DIRECT_TO_DECIMAL -> 1 // and for recycler view for sum fragment
            CONVERSATION -> 2   // for fragment with conversation
            else -> 0   // FROM_INVERSION_TO_DIRECT, FROM_ADDITIONAL_TO_INVERSION (sum fragment)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ParentNumberViewHolder {
        this.viewType = viewType
        return when (viewType) {
            0 -> SumBinaryViewHolder.create(inflater, viewGroup, listener, launchMode)
            1 -> SumDecimalViewHolder.create(inflater, viewGroup, listener, launchMode)
            else -> ConvBinaryViewHolder.create(inflater, viewGroup, listener, launchMode)
        }
    }

    override fun onBindViewHolder(holder: ParentNumberViewHolder, position: Int) {
        return when (holder.itemViewType) {
            0 -> (holder as SumBinaryViewHolder).bindTo(items[position], isLast(position), invalidatePosition)
            1 -> (holder as SumDecimalViewHolder).bindTo(items[position], isLast(position), invalidatePosition)
            else -> holder.bindTo(items[position], invalidatePosition)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setFocusedPosition(focusedPosition: Int){
        this.focusedPosition = focusedPosition
    }

    fun addField(customNumber: CustomNumber) {
        items.add(customNumber)
        this.notifyDataSetChanged()
    }

    fun getLast(): CustomNumber {
        return items[items.size - 1]
    }

    fun removeLastField() {
        if (itemCount > 0) {
            items.removeAt(itemCount - 1)
            this.notifyDataSetChanged()
        }
    }

    private fun isLast(position: Int): Boolean {
        for (i in items.size - 1 downTo 0)
            if (items[i].isPlusOperation())
                return position == i

        return false
    }

    fun updateNumber(numberString: String, position: Int) {
        if (position < items.size) {
            items[position].numberString = numberString
            invalidatePosition = -1
        }
    }

    fun setValidationError(position: Int?) {
        if(position != null)
            invalidatePosition = position
        this.notifyDataSetChanged()
    }

    fun updateDataForFocusedItem(keyBtn: KeyBtn, context: Context){
        if(focusedPosition == -1)
            return

        val view = layoutManager.findViewByPosition(focusedPosition)
        val numberField = view?.itemNumber?.number
        val customNumber = KeyboardAdapter.updateTextEdit(keyBtn, numberField, context)

        items[focusedPosition].numberString = customNumber.numberString

        // save start position in EditText
        if(keyBtn.typeButton != REINIT)
            items[focusedPosition].startPositionInEditText = customNumber.startPositionInEditText

        if(customNumber.comment != items[focusedPosition].comment &&    // if comment is changed
                viewType != 2 &&                                        // it is not conversation viewHolder (ConversationFragment)
                !items[focusedPosition].isTransformOperation()) {        // it is not transform field
            items[focusedPosition].comment = customNumber.comment
            this.notifyItemChanged(focusedPosition)
        }
    }
}

/**
 * Callback listener for keyboard
 */
interface NumberFieldListener {
    var notification: String                                // error/notification string

    /**
     * Change a focus
     * @hasFocus - a focus value
     * @focusedPosition - item position in recycler view
     */
    fun onFocus(hasFocus: Boolean, focusedPosition: Int)

    /**
     * Update item in recycler view
     * @numberString - binary/decimal code in String format
     * @position - number of item
     */
    fun updateItem(numberString: String, position: Int)
}