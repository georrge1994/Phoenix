package com.phoenix.kspt

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.crashlytics.android.Crashlytics
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.phoenix.kspt.models.User
import com.phoenix.kspt.firebase.FirebaseHelper
import io.fabric.sdk.android.Fabric

class Application : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var mContext: Context
        lateinit var firebase: FirebaseHelper
        lateinit var auth: FirebaseAuth
        lateinit var currentUser: User
    }

    override fun onCreate() {
        super.onCreate()
        firebase = FirebaseHelper()
        auth = FirebaseAuth.getInstance()
        currentUser = User()
        Fabric.with(this, Crashlytics())
        Fresco.initialize(applicationContext)

        mContext = this
    }
}