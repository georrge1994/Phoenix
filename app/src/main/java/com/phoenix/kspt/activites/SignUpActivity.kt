package com.phoenix.kspt.activites

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.phoenix.kspt.BuildConfig
import com.phoenix.kspt.R
import com.phoenix.kspt.utils.DEVELOP
import com.phoenix.kspt.utils.HelpFunctions
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUpActivity : AuthActivity() {
    private lateinit var password: String
    private lateinit var secret: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        setWindowColor(R.drawable.sign_up_gradient, R.color.transparent)

        dialog = HelpFunctions.initWaitingDialog(this, waitProgressbar)
        waitProgressbar.visibility = View.GONE

        // listeners
        signUpButton.setOnClickListener { createNewUser() }
        backButton.setOnClickListener { onBackPressed() }

        // editText action Done
        secretWord.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
                createNewUser()
            false
        }

        // only for develop flavor
        if (BuildConfig.FLAVOR.equals(DEVELOP))
            secretWord.setText(DEVELOP)
    }

    /**
     * Read data from field and push it to firebase
     */
    private fun createNewUser() {
        showIOSDialog(true)
        firstName = firstNameField.text.toString().trim()
        secondName = lastNameField.text.toString().trim()
        email = userStatus.text.toString().trim()
        password = passwordField.text.toString().trim()
        secret = secretWord.text.toString().trim()

        if (!validateForm()) {
            showIOSDialog(false)
            return
        }

        firebase.fetchSecretWord().subscribe {
            if (it == secret)
                auth.createUserWithEmailAndPassword(email!!, password).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        onAuthSuccess(task.result.user.uid)
                    } else {
                        showIOSDialog(false)
                        Toast.makeText(this@SignUpActivity, getString(R.string.registration_error), Toast.LENGTH_SHORT).show()
                    }
                }
            else {
                showIOSDialog(false)
                Toast.makeText(this, getString(R.string.incorrect_secret_word), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Check validation all field in registration form
     */
    private fun validateForm(): Boolean {
        return fieldIsValid(firstNameField, firstName!!, getString(R.string.input_your_first_name)) &&
                fieldIsValid(lastNameField, secondName!!, getString(R.string.input_your_last_name)) &&
                fieldIsValid(userStatus, email!!, getString(R.string.input_your_email)) &&
                fieldIsValid(passwordField, password, getString(R.string.input_your_password)) &&
                fieldIsValid(secretWord, secret, getString(R.string.Enter_key))
    }

    /**
     * Check validation data in the EditText
     */
    private fun fieldIsValid(editText: EditText, field: String, errorMsg: String): Boolean =
            if (field.isEmpty()) {
                editText.error = errorMsg
                false
            } else {
                editText.error = null
                true
            }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.left_animation_enter, R.anim.left_animation_leave)
    }
}
