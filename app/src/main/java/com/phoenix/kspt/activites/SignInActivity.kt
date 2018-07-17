package com.phoenix.kspt.activites

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.github.clans.fab.FloatingActionButton
import com.phoenix.kspt.R
import com.phoenix.kspt.utils.*
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignInActivity : AuthActivity() {
    /**
     *  Buttons types
     */
    enum class TypeOfFabs { SIGN_UP, SIGN_IN, CHANGE_PASSWORD }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        setWindowColor(R.drawable.sign_in_gradient, R.color.transparent)

        initFabMenu()
        dialog = HelpFunctions.initWaitingDialog(this, waitProgressbar)
        waitProgressbar.visibility = View.GONE

        // editText action Done
        passwordFieldView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE)
                signIn()
            false
        }
    }

    override fun onStart() {
        super.onStart()
        // Check auth on Activity start
        if (auth.currentUser != null && HelpFunctions.getCurrentUserId(this).isNotEmpty()) {
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /**
     * Restore an user's password ()
     */
    private fun restorePass() {
        // TODO: just do it!
    }

    /**
     * Show sign up activity
     */
    private fun signUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.right_animation_enter, R.anim.right_animation_leave)
        startActivity(intent, options.toBundle())
    }

    /**
     * Try to authorization and then open Main activity -> profile
     */
    private fun signIn() {
        showIOSDialog(true)
        val email = emailFieldView.text.toString().trim()
        val password = passwordFieldView.text.toString()

        if (!checkValidateForm(email, password)) {
            showIOSDialog(false)            // validation error
            return
        }

        fabMenu.close(true)
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                onAuthSuccess(task.result.user.uid)
            } else {
                showIOSDialog(false)
                Toast.makeText(this, getString(R.string.authorization_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Validation of the form
     */
    private fun checkValidateForm(email: String, password: String): Boolean {
        if (email == "") {
            Toast.makeText(this, getString(R.string.enter_email), Toast.LENGTH_SHORT).show()
            emailFieldView.error = getString(R.string.error_name)
            return false
        }

        if (password == "") {
            Toast.makeText(this, getString(R.string.input_your_password), Toast.LENGTH_SHORT).show()
            passwordFieldView.error = getString(R.string.error_password)
            return false
        }

        emailFieldView.error = null
        passwordFieldView.error = null

        return true
    }

    /**
     * Create fab menu
     */
    private fun initFabMenu(){
        addFabToMenu(getString(R.string.Registration), R.drawable.ic_person_add_24dp, TypeOfFabs.SIGN_UP)
        addFabToMenu(getString(R.string.change_password), R.drawable.ic_change_password_24dp, TypeOfFabs.CHANGE_PASSWORD)
        addFabToMenu(getString(R.string.enter), R.drawable.ic_arrow_forward_24dp, TypeOfFabs.SIGN_IN)
        fabMenu.getChildAt(1).isEnabled = false // disable "change password"
    }

    /**
     * Create button in fab menu
     */
    private fun addFabToMenu(name: String, imageId: Int, type: TypeOfFabs) {
        // Sign in
        val programFab = FloatingActionButton(this)
        programFab.buttonSize = FloatingActionButton.SIZE_MINI
        programFab.labelText = name
        programFab.colorNormal = ContextCompat.getColor(this, R.color.colorAccent)
        programFab.setImageResource(imageId)
        fabMenu.addMenuButton(programFab)

        when (type) {
            TypeOfFabs.SIGN_UP -> programFab.setOnClickListener { signUp() }
            TypeOfFabs.SIGN_IN -> programFab.setOnClickListener { signIn() }
            TypeOfFabs.CHANGE_PASSWORD -> programFab.setOnClickListener { restorePass() }
        }
    }
}
