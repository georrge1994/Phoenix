package com.phoenix.kspt.activites

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.View
import android.view.WindowManager
import com.gdacciaro.iOSDialog.iOSDialogBuilder
import com.gmail.samehadar.iosdialog.IOSDialog
import com.phoenix.kspt.fragments.MenuFragment
import com.phoenix.kspt.fragments.MenuListener
import com.phoenix.kspt.fragments.POSITION_OVT
import com.phoenix.kspt.R
import com.phoenix.kspt.utils.HelpFunctions
import com.phoenix.kspt.firebase.FireBaseActivity
import com.phoenix.kspt.profile.ProfileFragment
import com.phoenix.kspt.models.User
import com.phoenix.kspt.ovt.home.OvtFragment
import com.phoenix.kspt.people.PeopleFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Callback interface for using methods from main activity
 */
interface MainWindowInterface {
    /**
     * Show/Hide waiting progressbar
      */
    fun visibilityWait(enable: Boolean)

    /**
     * Init title in activity
     */
    fun setTitle(title: String)

    /**
     * Replace fragment in main frame
     */
    fun replaceFragment(fragment: Fragment, stackEnable: Boolean)
}

/**
 * Connecting activity. Here was initialized a drawable menu
 * and implemented several methods to switching fragment
 */
class MainActivity : FireBaseActivity(), MenuListener, MainWindowInterface {

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var menuFragment: MenuFragment
    private lateinit var waitingDialog: IOSDialog

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.statusBar)

        setSupportActionBar(toolbar)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu_24dp)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        waitingDialog = HelpFunctions.initWaitingDialog(this, waitProgressbar)
        initDrawer()
        showMyProfile()
    }

    /**
     * Initialize of the sidebar menu
     */
    private fun initDrawer() {
        menuFragment = MenuFragment()
        supportFragmentManager.beginTransaction()
                .replace(R.id.menuContainer, menuFragment, menuFragment.tag)
                .commit()

        drawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawerOpen, R.string.drawerClose) {}
        drawerToggle.isDrawerIndicatorEnabled = true
    }

    override fun showMyProfile() {
        val currentUser = User()
        currentUser.id = auth.currentUser!!.uid
        currentUser.firstName = HelpFunctions.getCurrentUserFirstName(this)
        currentUser.lastName = HelpFunctions.getCurrentUserLastName(this)
        currentUser.avatar = HelpFunctions.getCurrentUserAvatar(this)
        currentUser.groupId = HelpFunctions.getCurrentUserGroup(this)
        currentUser.userStatus = HelpFunctions.getCurrentUserStatus(this)
        currentUser.email = HelpFunctions.getCurrentUserEmail(this)

        MenuFragment.currentPosition = POSITION_OVT

        val homeFragment = ProfileFragment.newInstance(currentUser)
        replaceFragment(homeFragment, false)
    }

    override fun showOVT(userId: String) {
        replaceFragment(OvtFragment.newInstance(userId), true)
    }

    override fun showPeople() {
        replaceFragment(PeopleFragment(), true)
    }

    override fun replaceFragment(fragment: Fragment, stackEnable: Boolean) {
        drawerLayout.closeDrawers()
        val tr = supportFragmentManager.beginTransaction()
        tr.setCustomAnimations(R.anim.right_animation_enter, R.anim.right_animation_leave)
        tr.replace(R.id.mainContainer, fragment, fragment.tag)
        if (stackEnable)
            tr.addToBackStack(null)
        tr.commit()
    }

    override fun logout(silent: Boolean) {
        if (silent)
            simpleLogout()
        else
            iOSDialogBuilder(this)
                    .setTitle(getString(R.string.Are_you_sure_you_want_to_logout))
                    .setBoldPositiveLabel(true)
                    .setCancelable(false)
                    .setPositiveListener(getString(R.string.Yes)) { dialog ->
                        dialog.dismiss()
                        simpleLogout()
                    }.setNegativeListener(getString(R.string.No)) { dialog -> dialog.dismiss() }
                    .build().show()

    }

    /**
     * Log out from the Firebase and open an auth activity
     */
    private fun simpleLogout() {
        auth.signOut()
        HelpFunctions.getSharedPrefEditor(this).clear().apply()
        val intent = Intent(this, SignInActivity::class.java)
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
        startActivity(intent, options.toBundle())
        finish()
    }

    override fun visibilityWait(enable: Boolean) {
        if (enable) {
            waitProgressbar.visibility = View.VISIBLE
            waitingDialog.show()
        } else {
            waitProgressbar.visibility = View.GONE
            waitingDialog.hide()
        }
    }

    override fun setTitle(title: String) {
        supportActionBar!!.title = title
    }

    override fun onBackPressed() {
        val fragment = this.supportFragmentManager.findFragmentById(R.id.mainContainer)
        if (fragment is ProfileFragment)
            super.onBackPressed()
        else
            showMyProfile()
    }
}