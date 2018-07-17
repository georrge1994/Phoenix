package com.phoenix.kspt.firebase

import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.phoenix.kspt.Application
import com.phoenix.kspt.R

/**
 * Created by darkt on 1/7/2018.
 * Base activity for anything. Contain firebase objects firebase and @auth
 */
open class FireBaseActivity : AppCompatActivity() {
    protected var firebase: FirebaseHelper = Application.firebase
    protected var auth: FirebaseAuth = Application.auth

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            // after user click to back button in toolbar
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Init background colors for window
     */
    protected fun setWindowColor(windowColor: Int, statusBarColor: Int){
        val background = ContextCompat.getDrawable(this, windowColor)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, statusBarColor)
        window.navigationBarColor = ContextCompat.getColor(this, statusBarColor)
        window.setBackgroundDrawable(background)
    }


    override fun onBackPressed() {
        super.onBackPressed()
        // animation
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}