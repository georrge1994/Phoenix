package com.phoenix.kspt.utils

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.phoenix.kspt.firebase.FirebaseFragment
import com.phoenix.kspt.ovt.theme_one.fragments.NumbersFragment

class ScreenSlidePagerAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager) {
    private var fragmentsList: ArrayList<FirebaseFragment> = ArrayList()

    constructor(manager: FragmentManager, fragmentsList: ArrayList<FirebaseFragment>) : this(manager) {
        this.fragmentsList = fragmentsList
    }

    override fun getItem(position: Int): FirebaseFragment {
        return fragmentsList[position]
    }

    fun getItemAsParentFragment(position: Int): NumbersFragment {
        return fragmentsList[position] as NumbersFragment
    }

    override fun getCount(): Int {
        return fragmentsList.size
    }

    fun getLastItem(): FirebaseFragment? {
        return if (count > 0)
            fragmentsList[count - 1]
        else
            null
    }
}