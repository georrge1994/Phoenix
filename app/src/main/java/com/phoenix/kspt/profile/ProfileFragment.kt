package com.phoenix.kspt.profile


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.gdacciaro.iOSDialog.iOSDialogBuilder
import com.phoenix.kspt.Application.Companion.firebase
import com.phoenix.kspt.BuildConfig
import com.phoenix.kspt.activites.MainWindowInterface
import com.phoenix.kspt.fragments.MenuListener
import com.phoenix.kspt.R
import com.phoenix.kspt.models.User
import com.phoenix.kspt.people.PeopleFragment
import com.phoenix.kspt.utils.*
import kotlinx.android.synthetic.main.fragment_profile.*
import rx.Observable
import java.util.ArrayList
import kotlin.collections.HashSet
import kotlin.collections.isNotEmpty
import kotlin.collections.sortedWith

/**
 * Home page. Contain user avatar
 */
class ProfileFragment : PicturesFragment() {
    private var user: User = User()
    private var itsMe: Boolean = false
    private var listener: MainWindowInterface? = null
    private var menuListener: MenuListener? = null
    private var allUsers: ArrayList<User> = ArrayList()
    private lateinit var adapter: ProfileRecyclerViewAdapter

    companion object {
        fun newInstance(user: User): Fragment {
            val fragment = ProfileFragment()
            val bundle = Bundle()
            bundle.putParcelable(USER, user)
            fragment.arguments = bundle
            return fragment
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener?.setTitle("")

        if (arguments != null) {
            user = arguments!!.getParcelable(USER)
            itsMe = user.id == HelpFunctions.getCurrentUserId(context!!)
        }

        // show the needed fields
        when{
            user.userStatus == PROFESSOR && itsMe -> {
                existingCourses.visibility = View.VISIBLE
                recyclerView.visibility = View.VISIBLE
                existingCourses.text = getString(R.string.existing_groups)
                professSettings.visibility = View.VISIBLE
                initProfessorLayout()
            }
            user.userStatus == PROFESSOR && !itsMe -> {
                existingCourses.visibility = View.GONE
                recyclerView.visibility = View.GONE
                professSettings.visibility = View.GONE
            }
            user.userStatus == STUDENT -> {
                existingCourses.text = getString(R.string.available_courses)
                initRecyclerViewForStudent()
            }
        }

        initFields()

        // enable the needed fields
        if(itsMe){
            // init VIEW for uploading img
            this.avatarImg = avatar

            // init menu for uploading img
            initPopupTransformationMenu()

            // init the optional listeners
            saveProfileBtn.setOnClickListener { saveUpdates() }
            uploadPhotoBtn.setOnClickListener {
                powerMenu.showAsDropDown(view, view.rootView.measuredWidth / 2 - powerMenu.contentViewWidth / 2,
                        -view.rootView.measuredHeight / 2 - powerMenu.contentViewHeight / 2)
            }

            // editText action Done
            groupField.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    saveUpdates()
                false
            }
        }

        // init the required listeners
        removeProfileBtn.setOnClickListener {
            if (BuildConfig.FLAVOR.equals(DEVELOP))
                Toast.makeText(context,getString(R.string.Sorry_you_cant_remove_user), Toast.LENGTH_LONG).show()
            else
                removeUser()
        }
        listener?.visibilityWait(false)

        // only for develop flavor
        if(BuildConfig.FLAVOR.equals(DEVELOP))
            secretWord.isEnabled = false
    }

    @SuppressLint("SetTextI18n")
    private fun initFields() {
        avatar.setImageURI(user.avatar)                                 // avatar
        bigNameField.setText("${user.firstName} ${user.lastName}")      // name
        mailField.setText(user.email)                                   // email

        when{
            user.userStatus == STUDENT -> {
                groupField.setText(user.groupId)
                groupField.isEnabled = itsMe
                professSettings.visibility = View.GONE
            }
            user.userStatus == PROFESSOR -> {
                groupField.setText(getString(R.string.professor_group))
                groupField.isEnabled = false
                if(itsMe) {
                    professSettings.visibility = View.VISIBLE
                    clearFirebaseBtn.setOnClickListener {
                        if (BuildConfig.FLAVOR.equals(DEVELOP))
                            Toast.makeText(context,getString(R.string.Sorry_you_cant_clear_firebase), Toast.LENGTH_LONG).show()
                        else
                            clearFirebase()
                    }
                }
            }
        }

        setEnable(itsMe)

        listener!!.visibilityWait(false)                        // hide a waiting dialog
    }

    private fun setEnable(enable: Boolean) {
        bigNameField.isEnabled = enable
        groupField.isEnabled = enable
        mailField.isEnabled = enable
        uploadPhotoBtn.visibility = if (enable) View.VISIBLE else View.GONE
        saveProfileBtn.visibility = if (enable) View.VISIBLE else View.GONE

        // Only professor can remove people. Professor can't remove self
        removeProfileBtn.visibility = when{
            HelpFunctions.getCurrentUserStatus(context!!) == STUDENT -> View.GONE
            !itsMe && HelpFunctions.getCurrentUserStatus(context!!) == PROFESSOR ->  View.VISIBLE
            else -> View.GONE
        }
    }

    private fun saveUpdates() {
        val updatedUser = User()
        updatedUser.id = HelpFunctions.getCurrentUserId(context!!)

        val fullName = bigNameField.text.toString().trim()
        if (fullName.isEmpty()) {
            Toast.makeText(context, getString(R.string.Name_can_not_be_empty), Toast.LENGTH_SHORT).show()
            return
        } else {
            val array = fullName.split(" ")
            if (array.isNotEmpty())
                updatedUser.firstName = array[0]

            if (array.size > 1)
                updatedUser.lastName = array[1]
        }

        updatedUser.avatar = this.user.avatar
        updatedUser.email = mailField.text.toString()

        if (this.user.userStatus == PROFESSOR) {
            firebase.pushNewSecretRegistrationWord(secretWord.text.toString())
            updatedUser.userStatus = PROFESSOR
        } else {
            updatedUser.groupId = groupField.text.toString()
            updatedUser.userStatus = STUDENT
        }

        // update shared for correct view
        val sharedPrefEditor = HelpFunctions.getSharedPrefEditor(context!!)
        sharedPrefEditor.remove(USER_FIRST_NAME)
        sharedPrefEditor.putString(USER_FIRST_NAME, updatedUser.firstName)
        sharedPrefEditor.remove(USER_LAST_NAME)
        sharedPrefEditor.putString(USER_LAST_NAME, updatedUser.lastName)
        sharedPrefEditor.remove(USER_EMAIL)
        sharedPrefEditor.putString(USER_EMAIL, updatedUser.email)
        sharedPrefEditor.remove(USER_GROUP)
        sharedPrefEditor.putString(USER_GROUP, updatedUser.groupId)
        sharedPrefEditor.apply()

        firebase.pushNewUser(updatedUser)

        saveAvatar()
        Toast.makeText(context, getString(R.string.profile_successfully_updated), Toast.LENGTH_SHORT).show()
    }

    private fun initRecyclerViewForStudent() {
        val courseList: ArrayList<String> = ArrayList()
        courseList.add(getString(R.string.OVT_long))        // only ovt

        adapter = ProfileRecyclerViewAdapter(context!!, courseList, object : SimpleRecyclerViewClickListener {
            override fun openIt(groupName: String) {
                adapter.notifyDataSetChanged()
                menuListener?.showOVT(user.id)
            }
        })
        recyclerView.adapter = adapter
    }

    private fun initProfessorLayout() {
        adapter = ProfileRecyclerViewAdapter(context!!, getGroupsList(ArrayList()), object : SimpleRecyclerViewClickListener {
            override fun openIt(groupName: String) {
                adapter.notifyDataSetChanged()
                listener?.replaceFragment(PeopleFragment.newInstance(allUsers, groupName), false)
            }
        })
        recyclerView.adapter = adapter
        listener?.visibilityWait(true)

        Observable.zip(firebase.fetchAllUsers(),
                firebase.fetchSecretWord()) { users, secret ->

                    if (secret != null)
                        secretWord.setText(secret)

                    if (users != null) {
                        allUsers.clear()
                        allUsers.addAll(users.sortedWith(PeopleFragment.CompareUsers))
                        listener?.visibilityWait(false)
                        adapter.addAll(getGroupsList(allUsers))
                    }
                    users
                }.subscribe {
                    // don't remove it. Observable should have a subscriber
                    println("************  user count " + it.size)
                }
    }

    private fun getGroupsList(people: ArrayList<User>): ArrayList<String> {
        val setGroups = HashSet<String>()
        for (human in people)
            setGroups.add(human.groupId)

        setGroups.remove(User().groupId)                          // remove people without groups
        setGroups.remove(getString(R.string.professor_group))     // remove professors

        return ArrayList(setGroups)
    }

    private fun removeUser() {
        iOSDialogBuilder(context)
                .setTitle(if (HelpFunctions.getCurrentUserId(context!!) == user.id)
                    getString(R.string.are_sure_remove_you)
                else
                    getString(R.string.are_sure_remove_it_user))
                .setSubtitle(getString(R.string.All_data_will_be_lost))
                .setBoldPositiveLabel(true)
                .setCancelable(false)
                .setPositiveListener(getString(R.string.Yes)) { dialog ->
                    firebase.removeUser(user.id)
                    if (HelpFunctions.getCurrentUserId(context!!) == user.id) {
                        menuListener?.logout(true)
                    } else {
                        activity?.onBackPressed()
                    }
                    dialog.dismiss()
                }
                .setNegativeListener(getString(R.string.No)) { dialog -> dialog.dismiss() }
                .build().show()
    }

    private fun clearFirebase() {
        iOSDialogBuilder(context)
                .setTitle(getString(R.string.are_sure_clear_firebase))
                .setSubtitle(getString(R.string.All_student_data_will_be_lost))
                .setBoldPositiveLabel(true)
                .setCancelable(false)
                .setPositiveListener(getString(R.string.Yes)) { dialog ->
                    firebase.globalRemoveData(false)
                    dialog.dismiss()
                }
                .setNegativeListener(getString(R.string.No)) { dialog -> dialog.dismiss() }
                .build().show()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (listener == null && context is MainWindowInterface)
            listener = context

        if (menuListener == null && context is MenuListener)
            menuListener = context
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        menuListener = null
    }
}
