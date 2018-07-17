package com.phoenix.kspt.ovt.theme_one.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.phoenix.kspt.Application.Companion.mContext
import com.phoenix.kspt.R
import com.phoenix.kspt.ovt.models.CustomNumber
import com.phoenix.kspt.ovt.models.Stage
import com.phoenix.kspt.ovt.theme_one.BinaryFunctions
import com.phoenix.kspt.ovt.theme_one.adapters.NumberFieldListener
import com.phoenix.kspt.ovt.theme_one.adapters.RecyclerViewNumbersAdapter
import com.phoenix.kspt.utils.*
import kotlinx.android.synthetic.main.ovt_1_fragment_conversation.*
import java.util.*

private const val NUMBER: String = "number"
private const val LETTER: String = "letter"

class ConversationFragment : NumbersFragment() {
    private var correctAnswers: ArrayList<CustomNumber> = ArrayList()
    private var letter: String = "A"
    private var number = 0.0
    private var minusNumber = 0.0

    private var listIsInit = false

    companion object {
        fun newInstance(number: Double, letter: String, stage: Stage?): ConversationFragment {
            val fragment = ConversationFragment()
            val args = Bundle()
            args.putDouble(NUMBER, number)
            args.putString(LETTER, letter)
            if(stage != null)
                args.putParcelable(STAGE, stage)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            number = arguments!!.getDouble(NUMBER)
            letter = arguments!!.getString(LETTER)
            minusNumber = -number

            stage = if(arguments!!.containsKey(STAGE))
                arguments!!.getParcelable(STAGE)            // student's answers mode
            else
                Stage(getCorrectAnswers(number, letter))    // correct answers mode for professor
        }

        // init items
        if(!listIsInit) {
            items.add(CustomNumber("", CONVERSATION, "$letter ${getString(R.string.direct)}"))
            items.add(CustomNumber("", CONVERSATION, "$letter ${getString(R.string.inversion)}"))
            items.add(CustomNumber("", CONVERSATION, "$letter ${getString(R.string.additional)}"))
            items.add(CustomNumber("", CONVERSATION, "-$letter ${getString(R.string.direct)}"))
            items.add(CustomNumber("", CONVERSATION, "-$letter ${getString(R.string.inversion)}"))
            items.add(CustomNumber("", CONVERSATION, "-$letter ${getString(R.string.additional)}"))
            listIsInit = true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ovt_1_fragment_conversation, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // task text
        val html = String.format(Locale.getDefault(), ("<html>\n" +
                "<body style=\"text-align:justify;color:gray;background-color:white;font-style: italic;\">" +
                getString(R.string.task_text_course1_stage1) +
                "</body>\n" +
                "</html>"))
        taskText.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        // number and letter
        letterField.text = "$letter = $number"

        if (launchMode != PROFESSOR_MODE)
            taskText.visibility = View.VISIBLE     // hide task for professor mode

        correctAnswers = getCorrectAnswers(number, letter)

        // init adapter and recyclerView
        recyclerViewAdapter = RecyclerViewNumbersAdapter(context!!, items, object : NumberFieldListener {
                    override var notification: String
                        get() = when (notificationMode) {
                            ERROR_RESULT -> getString(R.string.incorrect_number)
                            INCORRECT_FORMAT -> getString(R.string.incorrect_format)
                            else -> getString(R.string.incorrect_number)
                        }
                        set(value) {}

                    override fun onFocus(hasFocus: Boolean, focusedPosition: Int) {
                        recyclerViewAdapter?.setFocusedPosition(focusedPosition)     // save focused position
                        keyboardListener?.changingBottomBar(hasFocus)           // show/hide keyboard
                    }

                    override fun updateItem(numberString: String, position: Int) {
                        recyclerViewAdapter?.updateNumber(numberString, position)
                    }
                },
                launchMode,
                recyclerView.layoutManager as LinearLayoutManager)
        recyclerView.adapter = recyclerViewAdapter

        // listeners for show/hide fabMenu and keyboard
        recyclerView.setOnTouchListener { _, event ->
            if(event.actionMasked == MotionEvent.ACTION_DOWN){
                keyboardListener?.changingBottomBar(false)
                true
            } else
                false
        }

        linearLayout.setOnClickListener {
            keyboardListener?.changingBottomBar(false)
        }

        // set user answers into EditTexts (setting only after a initialization the adapter)
        initStudentAnswers()
    }


    private fun getCorrectAnswers(number: Double, letter: String): ArrayList<CustomNumber> {
        val minusNumber = -number
        val directA = BinaryFunctions.getDirectCode(number, letter, context!!)
        val inversionA = BinaryFunctions.getInversionCode(directA, letter, context!!)
        val additionalA = BinaryFunctions.getAdditionalCode(directA, letter, context!!)
        val directB = BinaryFunctions.getDirectCode(minusNumber, "-$letter", context!!)
        val inversionB = BinaryFunctions.getInversionCode(directB, "-$letter", context!!)
        val additionalB = BinaryFunctions.getAdditionalCode(directB, "-$letter", context!!)

        directA.typeOperation = CONVERSATION
        inversionA.typeOperation = CONVERSATION
        additionalA.typeOperation = CONVERSATION
        directB.typeOperation = CONVERSATION
        inversionB.typeOperation = CONVERSATION
        additionalB.typeOperation = CONVERSATION

        val correctAnswers = ArrayList<CustomNumber>()
        correctAnswers.add(directA)
        correctAnswers.add(inversionA)
        correctAnswers.add(additionalA)
        correctAnswers.add(directB)
        correctAnswers.add(inversionB)
        correctAnswers.add(additionalB)

        return correctAnswers
    }

    override fun verificationOfAnswers(withNotification: Boolean): Boolean {
        if (isCorrectFormat(withNotification, FROM_CONVERSATION)) {
            notificationMode = ERROR_RESULT

            // init BitSets
            for (item in items)
                item.numberSet = BinaryFunctions.getNumberFromString(item.numberString)

            // compare user numbers and correct stack
            for (i in 0 until correctAnswers.size) {
                // one of field has error
                if (correctAnswers[i] != items[i]) {
                    saveStage(false)
                    if (withNotification) {
                        recyclerViewAdapter?.setValidationError(i)
                        makeErrorMessage(getString(R.string.incorrect_format_in, (i + 1)), i)
                    }
                    stage.countAttempts++
                    return false
                }
            }

            saveStage(true)
            if (withNotification)
                makeSuccessfulMessage()
            return true

        }
        return false
    }


    fun getKeyBoardButtons(context: Context): ArrayList<CustomNumber> {
        val keyButtons = ArrayList<CustomNumber>()

        val directNumber = items[0].numberString
        if (BinaryFunctions.isValid(directNumber))
            keyButtons.add(BinaryFunctions.getCustomNumberFromString(directNumber, "$letter ${context.getString(R.string.direct)}"))

        val minusInversionNumber = items[4].numberString
        if (BinaryFunctions.isValid(minusInversionNumber))
            keyButtons.add(BinaryFunctions.getCustomNumberFromString(minusInversionNumber, "-$letter ${context.getString(R.string.inversion)}"))

        val minusAdditionalNumber = items[5].numberString
        if (BinaryFunctions.isValid(minusAdditionalNumber))
            keyButtons.add(BinaryFunctions.getCustomNumberFromString(minusAdditionalNumber, "-$letter ${context.getString(R.string.additional)}"))

        return keyButtons
    }

    override fun getTitle(): String {
        return if (launchMode == PROFESSOR_MODE)
            mContext.getString(R.string.Conversation_number) + ": " + letter
        else
            mContext.getString(R.string.Conversation_number)
    }
}
