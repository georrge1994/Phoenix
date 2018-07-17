package com.phoenix.kspt.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.phoenix.kspt.R

import com.phoenix.kspt.utils.HelpFunctions
import com.phoenix.kspt.utils.PROFESSOR
import com.phoenix.kspt.utils.STUDENT
import kotlinx.android.synthetic.main.fragment_menu.*

/**
 * Callback menu listener
 */
interface MenuListener {
    /**
     * Show a profile fragment
     */
    fun showMyProfile()

    /**
     * Show home page of the "Computer Basic Science"
     */
    fun showOVT(userId: String)

    /**
     * Show a user profile
     */
    fun showPeople()

    /**
     * Show the dialog menu with choice
     */
    fun logout(silent: Boolean)
}

const val POSITION_HOME = 0
const val POSITION_STUDENTS_LIST = 1
const val POSITION_OVT = 2
const val LOGOUT_POSITION = 3

/**
 * Fragment of sidebar menu
 */
class MenuFragment : Fragment() {
    private var listener: MenuListener? = null

    companion object {
        var currentPosition: Int = POSITION_HOME
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNavigationViewClickListeners()
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        nameField.text = HelpFunctions.getCurrentUserFullName(context!!)
        val status = HelpFunctions.getCurrentUserStatus(context!!)
        val group = HelpFunctions.getCurrentUserGroup(context!!)
        if (status == STUDENT)
            userStatus.text = getString(R.string.User_status) + context!!.getString(R.string.Student) + " " + group
        else
            userStatus.text = getString(R.string.User_status)  + context!!.getString(R.string.Professor)

        val firstLettersInName = HelpFunctions.getFirstLettersUpperCase(
                HelpFunctions.getCurrentUserFirstName(context!!),
                HelpFunctions.getCurrentUserLastName(context!!))

        val backgroundColor = HelpFunctions.getBackgroundColorFor(
                HelpFunctions.getCurrentUserFullName(context!!))

        val bgShape = avatar.background as GradientDrawable
        bgShape.setColor(ContextCompat.getColor(context!!, backgroundColor))
        avatar.text = firstLettersInName

        if(HelpFunctions.getCurrentUserStatus(context!!) == PROFESSOR)
            navView.menu.findItem(R.id.drawer_students_list).isVisible = true
    }

    /**
     * Init menu navigation listener
     */
    private fun setupNavigationViewClickListeners() {
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.drawer_home -> {
                    listener?.showMyProfile()
                    currentPosition = POSITION_HOME
                }
                R.id.drawer_ovt -> {
                    listener?.showOVT(HelpFunctions.getCurrentUserId(context!!))
                    currentPosition = POSITION_OVT
                }
                R.id.drawer_students_list -> {
                    listener?.showPeople()
                    currentPosition = POSITION_STUDENTS_LIST
                }
                R.id.drawer_logout -> {
                    listener?.logout(false)
                    currentPosition = LOGOUT_POSITION
                }
            }
            true
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MenuListener)
            listener = context
        else
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
