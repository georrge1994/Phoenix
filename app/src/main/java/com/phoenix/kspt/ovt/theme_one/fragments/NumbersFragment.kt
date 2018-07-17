package com.phoenix.kspt.ovt.theme_one.fragments

import android.content.Context
import android.widget.Toast
import com.phoenix.kspt.R
import com.phoenix.kspt.firebase.FirebaseFragment
import com.phoenix.kspt.ovt.models.CustomNumber
import com.phoenix.kspt.ovt.models.Stage
import com.phoenix.kspt.ovt.theme_one.BinaryFunctions
import com.phoenix.kspt.ovt.theme_one.activites.ChangeBottomListeners
import com.phoenix.kspt.ovt.theme_one.adapters.RecyclerViewNumbersAdapter
import com.phoenix.kspt.utils.*
import java.util.ArrayList

/**
 * Created by darkt on 3/23/2018.
 */

const val STAGE: String = "stage"

open class NumbersFragment : FirebaseFragment() {
    protected var notificationMode: String = ERROR_RESULT
    protected val items: ArrayList<CustomNumber> = ArrayList()                   // EditText list
    var keyboardListener: ChangeBottomListeners? = null
    var recyclerViewAdapter: RecyclerViewNumbersAdapter? = null
    var stage: Stage = Stage()

    companion object {
        var numberA: Double = 0.0
        var numberB: Double = 0.0
        var launchMode: String = PREPARE_MODE
    }

    open fun verificationOfAnswers(withNotification: Boolean): Boolean {
        return false
    }

    protected fun saveStage(isCompleted: Boolean) {
        stage.stageCompleted = isCompleted
        stage.userAnswers.clear()           // clear old answer if exist
        for (item in items)
            stage.userAnswers.add(item)     // custom numbers list
    }

    fun makeErrorMessage(string: String, errorPosition: Int?) {
        recyclerViewAdapter?.setValidationError(errorPosition)
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show()
    }

    fun makeSuccessfulMessage(){
        var notificationMessage = getString(R.string.you_took) + " " + stage.countAttempts

        notificationMessage += when{
            stage.countAttempts == 1 -> getString(R.string.attempts1)
            stage.countAttempts < 5 -> getString(R.string.attempt234)
            stage.countAttempts >= 5  -> getString(R.string.attempts5n)
            else -> " " + getString(R.string.attempts5n)
        }

        Toast.makeText(context, notificationMessage, Toast.LENGTH_LONG).show()
    }

    protected fun initStudentAnswers() {
        // init textEdits
        if (launchMode == PROFESSOR_MODE || launchMode == CONTROL_MODE) {
            for (i in 0 until stage.userAnswers.size) {
                if (items.size > i)
                    items[i] = stage.userAnswers[i]
                else
                    recyclerViewAdapter?.addField(stage.userAnswers[i])
            }
        }
    }

    fun isCorrectFormat(withNotification: Boolean, mode: String): Boolean {
        notificationMode = INCORRECT_FORMAT

        var correction = 0
        if(mode == FROM_SUMMATION)
            correction = 1

        for (i in 0 until items.size - correction) // TODO: -1 it is patch for decimal number. D.N. not valid binary
            if (BinaryFunctions.isNotValid(items[i].numberString)) {
                saveStage(false)

                if (withNotification)
                    makeErrorMessage(getString(R.string.incorrect_format_in, (i + 1)), i)
                return false
            }
        return true
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (keyboardListener == null && context is ChangeBottomListeners)
            keyboardListener = context
    }

    override fun onDetach() {
        super.onDetach()
        keyboardListener = null
    }
}
