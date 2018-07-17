package com.phoenix.kspt.activites

import android.app.ActivityOptions
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.widget.Toast
import com.gmail.samehadar.iosdialog.IOSDialog
import com.phoenix.kspt.R
import com.phoenix.kspt.firebase.FireBaseActivity
import com.phoenix.kspt.models.User
import com.phoenix.kspt.utils.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import rx.Observable

/**
 * Base class for authorization and authentication
 */
open class AuthActivity : FireBaseActivity() {
    protected lateinit var dialog: IOSDialog

    protected var firstName: String? = null
    protected var secondName: String? = null
    protected var email: String? = null

    /**
     * Fetch the user-object by id and system variables from Firebase
     * @userId - the unique user's key given Firebase-auth
     */
    protected fun onAuthSuccess(userId: String) {
        Observable.zip(firebase.fetchUserById(userId).first(),
                firebase.fetchSystemVars()) { user, systemVars ->

                    showIOSDialog(false)

                    if(systemVars == null)
                        Toast.makeText(this, getString(R.string.Could_not_copy_system_variables), Toast.LENGTH_LONG).show()
                    else {
                        // save system vars to shared memory
                        val sharedPrefEditor = HelpFunctions.getSharedPrefEditor(this)
                        sharedPrefEditor.putString(OVT_THEME_1_URL, systemVars.ovtTheme1URL)
                        sharedPrefEditor.putString(OVT_THEME_2_URL, systemVars.ovtTheme2URL)
                        sharedPrefEditor.putString(OVT_THEME_3_URL, systemVars.ovtTheme3URL)
                        sharedPrefEditor.putString(OVT_THEME_4_URL, systemVars.ovtTheme4URL)
                        sharedPrefEditor.putString(AVATAR_DEFAULTS, systemVars.avatarDefault)
                        sharedPrefEditor.apply()
                    }

                    if(user == null) {
                        // that user is new (sing up activity)
                        if(firstName != null && secondName != null && email!= null) {
                            val newUser = User(firstName!!, secondName!!, email!!)
                            newUser.id = userId
                            saveUserAndStart(newUser, true)
                        // else it is mean this request from sign-activity. If user field == null then he is removed from system
                        } else {
                            Toast.makeText(this, getString(R.string.you_are_removed_from_firebase), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        user.id = userId
                        saveUserAndStart(user, false)
                    }

                }.subscribe {
                    // don't remove it. Observable should have a subscriber
                    println(it.hashCode())
                }
    }

    /**
     * Save user to shared memory (+ push to Firebase) and redirect to Profile
     */
    private fun saveUserAndStart(user: User, isNew: Boolean){
        // save user's data to shared memory
        val sharedPrefEditor = HelpFunctions.getSharedPrefEditor(this)
        sharedPrefEditor.putString(USER_ID, user.id)
        sharedPrefEditor.putString(USER_FIRST_NAME, user.firstName)
        sharedPrefEditor.putString(USER_LAST_NAME, user.lastName)
        sharedPrefEditor.putString(USER_EMAIL, user.email)
        sharedPrefEditor.putString(USER_GROUP, user.groupId)
        sharedPrefEditor.putString(USER_STATUS, user.userStatus)
        sharedPrefEditor.putString(USER_AVATAR, if(isNew)
                HelpFunctions.getDefaultAvatar(this)
            else
                user.avatar
        )
        sharedPrefEditor.apply()

        if (isNew)
            firebase.pushNewUser(user)

        showIOSDialog(false)

        // Go to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.abc_fade_in, R.anim.abc_fade_out)
        startActivity(intent,options.toBundle())
        finish()
    }

    /**
     * Show waiting scrollbar
     */
    protected fun showIOSDialog(enable: Boolean) {
        if (enable) {
            waitProgressbar.visibility = View.VISIBLE
            dialog.show()
        } else {
            waitProgressbar.visibility = View.GONE
            dialog.hide()
        }
    }
}
