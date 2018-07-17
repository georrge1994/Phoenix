package com.phoenix.kspt.utils

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.gmail.samehadar.iosdialog.IOSDialog
import com.phoenix.kspt.R
import java.util.*

/**
 * Created by darkt on 1/16/2018.
 */
enum class NumberType { POSITIVE_DOUBLE, NEGATIVE_DOUBLE, POSITIVE_INTEGER, NEGATIVE_INTEGER }

class HelpFunctions {
    companion object {

        private const val SHARED_PREF_NAME = "com.phoenix.kspt"

        private fun getSharedPref(context: Context): SharedPreferences {
            return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        }

        fun getSharedPrefEditor(context: Context): SharedPreferences.Editor {
            return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).edit()
        }

        fun getCurrentUserId(context: Context): String {
            return getSharedPref(context).getString(USER_ID, "User")
        }

        fun getCurrentUserFirstName(context: Context): String {
            return getSharedPref(context).getString(USER_FIRST_NAME, "User")
        }

        fun getCurrentUserLastName(context: Context): String {
            return getSharedPref(context).getString(USER_LAST_NAME, "User")
        }

        fun getCurrentUserFullName(context: Context): String {
            return getSharedPref(context).getString(USER_FIRST_NAME, "User") + " " +
                    getSharedPref(context).getString(USER_LAST_NAME, "")
        }

        fun getCurrentUserEmail(context: Context): String {
            return getSharedPref(context).getString(USER_EMAIL, "User@mail.com")
        }

        fun getCurrentUserGroup(context: Context): String {
            return getSharedPref(context).getString(USER_GROUP, "9000/1")
        }

        fun getCurrentUserStatus(context: Context): String {
            return getSharedPref(context).getString(USER_STATUS, STUDENT)
        }

        fun getDefaultAvatar(context: Context): String {
            return getSharedPref(context).getString(AVATAR_DEFAULTS, "nope")
        }

        fun getCurrentUserAvatar(context: Context): String {
            return getSharedPref(context).getString(USER_AVATAR, getDefaultAvatar(context))
        }

        fun getFileURL(context: Context, name: String): String{
            return getSharedPref(context).getString(name, "nope")
        }

        fun rand(from: Int, to: Int): Int {
            val random = Random()
            return random.nextInt(to - from) + from
        }

        fun getRandomNumber(type: NumberType): Double {

            var number: Double = rand(0, 63).toDouble()

            if (type == NumberType.POSITIVE_DOUBLE || type == NumberType.NEGATIVE_DOUBLE) {
                if (rand(0, 10).toLong() % 2 == 0L)
                    number += 0.5
                if (rand(0, 10).toLong() % 2 == 0L)
                    number += 0.25
                if (rand(0, 10).toLong() % 5 >= 2L)
                    number += 0.125
            }

            if (type == NumberType.NEGATIVE_DOUBLE || type == NumberType.NEGATIVE_INTEGER)
                number *= -1

            return number
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        fun initToolbarSettings(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window = activity.window
                //val background = ContextCompat.getDrawable(activity, R.drawable.sign_in_gradient)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(activity, android.R.color.transparent)
                window.navigationBarColor = ContextCompat.getColor(activity, android.R.color.transparent)
                //window.setBackgroundDrawable(background)
            }
        }

        fun getFirstLettersUpperCase(firstName: String, lastName: String): String {
            return when {
                firstName.isNotEmpty() && lastName.isNotEmpty() -> (firstName[0].toString() + lastName[0].toString()).toUpperCase()
                firstName.isNotEmpty() && lastName.isEmpty() -> firstName[0].toString().toUpperCase()
                firstName.isEmpty() && lastName.isNotEmpty() -> lastName[0].toString().toUpperCase()
                else -> "NEMO"
            }
        }

        private fun getFirstLetter(string: String): String {
            return string[0].toString().toLowerCase()
        }

        fun getBackgroundColorFor(fullName: String): Int {
            val nameParts = fullName.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val alphabet = "abcdefghijklmnopqrstuvwxyz"

            var code1 = 0
            var code2 = 0

            if (nameParts.isNotEmpty() && nameParts[0].isNotEmpty())
                code1 = alphabet.indexOf(getFirstLetter(nameParts[0]))

            if (nameParts.size > 1 && nameParts[1].isNotEmpty())
                code2 = alphabet.indexOf(getFirstLetter(nameParts[1]))

            val colorCode = (code1 + code2) % 5

            when (colorCode) {
                0 -> return R.color.letterBackground0
                1 -> return R.color.letterBackground1
                2 -> return R.color.letterBackground2
                3 -> return R.color.letterBackground3
                4 -> return R.color.letterBackground4
            }

            return R.color.letterBackground2
        }

        fun initWaitingDialog(context: Context, waitProgressbar: View): IOSDialog {
            waitProgressbar.visibility = View.VISIBLE
            return IOSDialog.Builder(context)
                    .setSpinnerColorRes(R.color.disableColor)
                    .setOnCancelListener { waitProgressbar.visibility = View.GONE }
                    .setTitle(context.getString(R.string.please_wait))
                    .setTitleColorRes(R.color.grey)
                    .setCancelable(true)
                    .setMessageContentGravity(Gravity.END)
                    .build()
        }

        fun hasNavigationBar(resources: Resources): Boolean {
            val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
            return id > 0 && resources.getBoolean(id)
        }

        fun isFragmentVisible(fragment: Fragment): Boolean {
            val activity = fragment.activity
            val focusedView = fragment.view?.findFocus()
            return (activity != null
                    && focusedView != null
                    && focusedView == activity.window.decorView.findFocus())
        }
    }
}