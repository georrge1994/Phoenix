package com.phoenix.kspt.firebase

import android.support.v4.app.Fragment
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.phoenix.kspt.Application
import com.phoenix.kspt.R

/**
 * Created by darkt on 3/23/2018.
 * Base fragment for everyone fragment which working with firebase
 */
open class FirebaseFragment : Fragment() {
    protected lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Application.auth
    }

    open fun getTitle(): String {
        return getString(R.string.here_can_be_your_blurb)
    }
}
