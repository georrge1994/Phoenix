package com.phoenix.kspt.ovt.theme_one.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.phoenix.kspt.Application.Companion.mContext
import com.phoenix.kspt.R
import com.phoenix.kspt.ovt.models.CustomNumber
import com.phoenix.kspt.ovt.models.Stage
import com.phoenix.kspt.ovt.theme_one.BinaryFunctions
import com.phoenix.kspt.ovt.theme_one.BinaryFunctionsInterface.OperationsCode
import com.phoenix.kspt.ovt.theme_one.BinaryFunctionsInterface.OperationsCode.*
import com.phoenix.kspt.ovt.theme_one.adapters.NumberFieldListener
import com.phoenix.kspt.ovt.theme_one.adapters.RecyclerViewNumbersAdapter
import com.phoenix.kspt.utils.ERROR_RESULT
import com.phoenix.kspt.utils.FROM_SUMMATION
import com.phoenix.kspt.utils.INCORRECT_FORMAT
import com.phoenix.kspt.utils.PROFESSOR_MODE
import kotlinx.android.synthetic.main.ovt_1_fragment_sum.*
import java.util.*

private const val OPERATION_CODE: String = "operation_code"

class SumFragment : BaseFragment() {
    // users
    private var correctAnswers: ArrayList<CustomNumber> = ArrayList()
    private var operationCode: OperationsCode = OperationsCode.A_PLUS_B         // operation code
    private var expression: String = "A + B"                                    // operation string

    init {
        items.add(CustomNumber())
        items.add(CustomNumber())
        items.add(CustomNumber())
        countBasicItems = 3
    }

    companion object {
        fun newInstance(operationCode: OperationsCode, stage: Stage?): SumFragment {
            val fragment = SumFragment()
            val bundle = Bundle()
            bundle.putSerializable(OPERATION_CODE, operationCode)
            if(stage != null)
                bundle.putParcelable(STAGE, stage)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            operationCode = arguments!!.get(OPERATION_CODE) as OperationsCode
            expression = when (operationCode) {
                A_PLUS_B -> getString(R.string.a_plus_b_additional)
                A_MINUS_B_ADDITIONAL -> getString(R.string.a_minus_b_additional)
                A_MINUS_B_INVERSION -> getString(R.string.a_minus_b_inversion)
                B_MINUS_A_ADDITIONAL -> getString(R.string.b_minus_a_additional)
                B_MINUS_A_INVERSION -> getString(R.string.b_minus_a_inversion)
            }

            stage = if(arguments!!.containsKey(STAGE))
                arguments!!.getParcelable(STAGE)                                            // student's answers mode
            else
                Stage(BinaryFunctions.getOperationsStack(numberA, numberB, operationCode, context!!))  // correct answers mode for professor
        }

        correctAnswers = BinaryFunctions.getOperationsStack(numberA, numberB, operationCode, context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ovt_1_fragment_sum, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Init a text of the task
        val html = String.format(Locale.getDefault(), ("<html>\n" +
                "<body style=\"text-align:justify;color:gray;background-color:white;font-style: italic;\">" +
                expression +
                "</body>\n" +
                "</html>"))
        taskText.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)

        if (launchMode != PROFESSOR_MODE)
            taskText.visibility = View.VISIBLE

        // init adapter
        recyclerViewAdapter = RecyclerViewNumbersAdapter(context!!, items, object : NumberFieldListener {
                    override var notification: String
                        get() = when (notificationMode) {
                            ERROR_RESULT -> getString(R.string.incorrect_number)
                            INCORRECT_FORMAT -> getString(R.string.incorrect_format)
                            else -> getString(R.string.incorrect_number)
                        }
                        set(_) {}

                    override fun onFocus(hasFocus: Boolean, focusedPosition: Int) {
                        keyboardListener?.changingBottomBar(hasFocus)                   // show/hide keyboard/check-button
                        recyclerViewAdapter?.setFocusedPosition(focusedPosition)        // save focused position
                        if(focusedPosition != -1)
                            fabMenu?.close(true)                                // hide fab menu
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
                fabMenu?.close(true)
                true
            } else
                false
        }

        linearLayout.setOnClickListener {
            keyboardListener?.changingBottomBar(false)
            fabMenu?.close(true)
        }

        // set user answers into EditTexts (setting only after a initialization the adapter)
        initStudentAnswers()
    }

    override fun getTitle(): String {
        val shortTask = when (operationCode) {
            A_PLUS_B -> getString(R.string.a_plus_b_result_additional)
            A_MINUS_B_ADDITIONAL -> getString(R.string.a_minus_b_result_additional)
            A_MINUS_B_INVERSION -> getString(R.string.a_minus_b_result_inversion)
            B_MINUS_A_ADDITIONAL -> getString(R.string.b_minus_a_result_additional)
            B_MINUS_A_INVERSION -> getString(R.string.b_minus_a_result_inversion)
        }
        return if (launchMode == PROFESSOR_MODE)
            mContext.getString(R.string.Adders) + ": " + shortTask
        else
            mContext.getString(R.string.Adders)
    }

    override fun verificationOfAnswers(withNotification: Boolean): Boolean {
        if (isCorrectFormat(withNotification, FROM_SUMMATION)) {
            notificationMode = ERROR_RESULT

            // init BitSets
            for (item in items)
                item.numberSet = BinaryFunctions.getNumberFromString(item.numberString)

            // compare user numbers and correct stack
            for (i in 0 until correctAnswers.size) {
                // the task is not fully accomplished
                if (items.size <= i) {
                    saveStage(false)
                    if (withNotification)
                        makeErrorMessage(getString(R.string.task_is_not_completed), null)
                    stage.countAttempts++
                    return false
                }

                // one of field has error
                if (correctAnswers[i] != items[i]) {
                    saveStage(false)
                    if (withNotification) {
                        recyclerViewAdapter?.setValidationError(i)
                        makeErrorMessage(getString(R.string.Error_in_, (i + 1)), i)
                    }
                    stage.countAttempts++
                    return false
                }
            }

            // Extra rows found
            if (correctAnswers.size < items.size) {
                saveStage(false)
                if (withNotification)
                    makeErrorMessage(getString(R.string.extra_rows_found), null)
                stage.countAttempts++
                return false
            }

            saveStage(true)
            if (withNotification)
                makeSuccessfulMessage()
            return true

        }
        return false
    }
}
