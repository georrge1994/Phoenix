package com.phoenix.kspt.ovt.theme_one.activites

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.WindowManager
import com.gdacciaro.iOSDialog.iOSDialogBuilder
import com.github.clans.fab.FloatingActionMenu
import com.phoenix.kspt.R
import com.phoenix.kspt.activites.MainActivity
import com.phoenix.kspt.firebase.FireBaseActivity
import com.phoenix.kspt.firebase.FirebaseFragment
import com.phoenix.kspt.keyboard.KeyboardAdapter
import com.phoenix.kspt.keyboard.PressKeyboardButton
import com.phoenix.kspt.models.KeyBtn
import com.phoenix.kspt.ovt.models.CustomNumber
import com.phoenix.kspt.ovt.models.StudentResults
import com.phoenix.kspt.ovt.theme_one.BinaryFunctionsInterface.OperationsCode
import com.phoenix.kspt.ovt.theme_one.fragments.ConversationFragment
import com.phoenix.kspt.ovt.theme_one.fragments.NumbersFragment
import com.phoenix.kspt.ovt.theme_one.fragments.ResultsFragment
import com.phoenix.kspt.ovt.theme_one.fragments.SumFragment
import com.phoenix.kspt.utils.*
import com.phoenix.kspt.utils.HelpFunctions.Companion.initToolbarSettings
import kotlinx.android.synthetic.main.ovt_1_activity_student.*


/**
 * Callback listener for triggering keyboard/check-button
 */
interface ChangeBottomListeners {
    /**
     * Show/hide the keyboard/check-button
     */
    fun changingBottomBar(keyboardVisible: Boolean)
}

/**
 * Callback listener for fab menu button
 */
interface FabMenuListener {
    /**
     * Return link to fab-menu-object
     */
    fun getFabMenu(): FloatingActionMenu
}

class OvtCourseActivity : FireBaseActivity(), ChangeBottomListeners, FabMenuListener {
    // System parameters
    private lateinit var keyboardAdapter: KeyboardAdapter
    private lateinit var currentFragment: NumbersFragment
    private lateinit var pagerAdapter: ScreenSlidePagerAdapter
    private val additionalKeyButtons: ArrayList<CustomNumber> = ArrayList()
    private var finished: Boolean = false

    // User data
    private lateinit var studentResults: StudentResults
    private var currentStage: Int = 1
    private var numberA = 0.0
    private var numberB = 0.0
    private var launchMode = PREPARE_MODE
    private var userId: String = ""

    companion object {
        fun startActivity(context: Context, studentResult: StudentResults, launchMode: String, userId: String) {
            val intent = Intent(context, OvtCourseActivity::class.java)

            intent.putExtra(STUDENT_RESULT, studentResult)
            intent.putExtra(LAUNCH_MODE, launchMode)
            intent.putExtra(STUDENT_ID, userId)

            val options = ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out)
            context.startActivity(intent, options.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ovt_1_activity_student)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // for phone without hard navigation button. This check is not work for emulators, only for real device
        if (HelpFunctions.hasNavigationBar(resources)) {
            bottomSpacer.visibility = View.VISIBLE
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }

        // init data
        studentResults = intent.getParcelableExtra(STUDENT_RESULT)
        launchMode = intent.getStringExtra(LAUNCH_MODE)
        userId = intent.getStringExtra(STUDENT_ID)
        if (launchMode == PREPARE_MODE) {
            numberA = HelpFunctions.getRandomNumber(NumberType.POSITIVE_DOUBLE)
            numberB = HelpFunctions.getRandomNumber(NumberType.POSITIVE_DOUBLE)
            checkButton.visibility = View.VISIBLE
        } else {
            numberA = studentResults.number1
            numberB = studentResults.number2
            checkButton.visibility = View.GONE
        }
        // init static val for fragments
        NumbersFragment.numberA = numberA
        NumbersFragment.numberB = numberB
        NumbersFragment.launchMode = launchMode

        // toolbar
        initToolbarSettings(this)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // a custom simple keyboard
        keyboardAdapter = KeyboardAdapter(this, object : PressKeyboardButton {
            override fun simpleClick(keyBtn: KeyBtn, context: Context) {
                currentFragment.recyclerViewAdapter?.updateDataForFocusedItem(keyBtn, context)
            }
        })
        val horizontalLayoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        keyboard.layoutManager = horizontalLayoutManager
        keyboard.adapter = keyboardAdapter

        // init viewpager with fragments
        initViewPager()

        // listeners
        checkButton.setOnClickListener { checkAnswers() }
        helpImage.setOnClickListener { helpImage.visibility = View.GONE }
        helpButton.setOnClickListener { showHelpImg() }
    }

    private fun checkAnswers() {
        when {
            currentStage == pagerAdapter.count -> finishAndUpload()                                   // when click in last stage
            launchMode == PREPARE_MODE -> currentFragment.verificationOfAnswers(true)  // preparation
        }
    }

    private fun initViewPager() {
        val fragments: ArrayList<FirebaseFragment> = ArrayList()
        fragments.add(ConversationFragment.newInstance(numberA, getString(R.string.letter_a), studentResults.stages[0]))
        fragments.add(ConversationFragment.newInstance(numberB, getString(R.string.letter_b), studentResults.stages[1]))
        fragments.add(SumFragment.newInstance(OperationsCode.A_PLUS_B, studentResults.stages[2]))
        fragments.add(SumFragment.newInstance(OperationsCode.A_MINUS_B_INVERSION, studentResults.stages[3]))
        fragments.add(SumFragment.newInstance(OperationsCode.B_MINUS_A_INVERSION, studentResults.stages[4]))
        fragments.add(SumFragment.newInstance(OperationsCode.A_MINUS_B_ADDITIONAL, studentResults.stages[5]))
        fragments.add(SumFragment.newInstance(OperationsCode.B_MINUS_A_ADDITIONAL, studentResults.stages[6]))
        fragments.add(ResultsFragment.newInstance(studentResults))

        pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager, fragments)
        pager.adapter = pagerAdapter

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                finished = false                                        // reset finish flag

                // refresh keyboard if required
                if (position == 1) {
                    additionalKeyButtons.clear()
                    keyboardAdapter.initKeyboard()
                } else if (position == 2) {
                    additionalKeyButtons.clear()
                    additionalKeyButtons.addAll((pagerAdapter.getItem(0) as ConversationFragment).getKeyBoardButtons(baseContext))
                    additionalKeyButtons.addAll((pagerAdapter.getItem(1) as ConversationFragment).getKeyBoardButtons(baseContext))
                    keyboardAdapter.initKeyboard(additionalKeyButtons)
                }

                // change check button text
                if (position == COUNT_STAGE - 1)
                    checkButton.text = applicationContext.getString(R.string.Give_me_mark)
                else
                    checkButton.text = applicationContext.getString(R.string.Check_it)

                // show fabMenuBtn
                if (position in 2..6)
                    fabMenu.visibility = View.VISIBLE
                else
                    fabMenu.visibility = View.GONE

                currentFragment = pagerAdapter.getItemAsParentFragment(position)        // update current fragment
                currentStage = position + 1                             // for title
                updateProgressBar()
                changingBottomBar(false)                            // hide keyboard
            }
        })

        currentFragment = pagerAdapter.getItemAsParentFragment(0)
        updateProgressBar()
    }

    private fun finishAndUpload() {
        if (finished) {
            MainActivity.startActivity(this)
            return
        } else {
            finished = true
        }

        // check answers and update stages
        for (i in 0 until pagerAdapter.count - 1) {
            pagerAdapter.getItemAsParentFragment(i).verificationOfAnswers(false)
            studentResults.stages[i] = pagerAdapter.getItemAsParentFragment(i).stage
        }
        // init page result
        (pagerAdapter.getLastItem() as ResultsFragment).updateResultPage(studentResults)

        // changed label for check button
        checkButton.text = applicationContext.getString(R.string.Finished)

        // write result to firebase
        if (launchMode == CONTROL_MODE) {
            // disable swipe
            pager.disableScroll(true)
            studentResults.percent = (pagerAdapter.getLastItem() as ResultsFragment).percent
            firebase.pushStudentAnswersTaskOne(HelpFunctions.getCurrentUserId(this), studentResults)
        }

        studentView.setOnClickListener { changingBottomBar(false) }
    }

    private fun updateProgressBar() {
        supportActionBar!!.title = getString(R.string.Step) + " $currentStage. ${currentFragment.getTitle()}"
        val progress = currentStage * 100 / COUNT_STAGE
        progressBar.progress = progress
    }

    override fun changingBottomBar(keyboardVisible: Boolean) {
        when (launchMode) {
            PROFESSOR_MODE -> {
                checkButton.visibility = View.GONE
                keyboard.visibility = View.GONE
            }
            PREPARE_MODE -> {
                keyboard.visibility = if(keyboardVisible) View.VISIBLE else View.GONE
                checkButton.visibility = if(keyboardVisible) View.GONE else View.VISIBLE
            }
            CONTROL_MODE -> {
                keyboard.visibility = if(keyboardVisible) View.VISIBLE else View.GONE
                checkButton.visibility = if(currentStage != COUNT_STAGE) View.GONE else View.VISIBLE
            }
        }
    }

    private fun showHelpImg(){
        helpImage.background = this.getDrawable(when (currentStage) {
            1 -> R.drawable.help_ovt_theme_1_step_1
            2 -> R.drawable.help_ovt_theme_1_step_2
            3 -> R.drawable.help_ovt_theme_1_step_3
            4 -> R.drawable.help_ovt_theme_1_step_4
            5 -> R.drawable.help_ovt_theme_1_step_5
            6 -> R.drawable.help_ovt_theme_1_step_6
            7 -> R.drawable.help_ovt_theme_1_step_7
            8 -> R.drawable.help_ovt_theme_1_step_8
            else -> R.drawable.help_default
        })
        helpImage.visibility = View.VISIBLE
    }

    override fun getFabMenu(): FloatingActionMenu {
        return fabMenu
    }

    override fun onBackPressed() {
        if (helpImage.visibility == View.VISIBLE) {
            helpImage.visibility = View.GONE

        } else if (keyboard.visibility == View.VISIBLE) {
            changingBottomBar(false)

        } else {
            if (finished) {
                overridePendingTransition(R.anim.left_animation_leave, R.anim.right_animation_enter)
                super.onBackPressed()
            } else {
                iOSDialogBuilder(this)
                        .setTitle(getString(R.string.Are_you_sure_you_want_to_abort_the_test))
                        .setSubtitle(getString(R.string.All_data_will_be_lost))
                        .setBoldPositiveLabel(true)
                        .setCancelable(false)
                        .setPositiveListener(getString(R.string.Yes)) { dialog ->
                            overridePendingTransition(R.anim.left_animation_leave, R.anim.right_animation_enter)
                            super.onBackPressed()
                            dialog.dismiss()
                        }
                        .setNegativeListener(getString(R.string.No)) { dialog -> dialog.dismiss() }
                        .build().show()
            }
        }
    }
}

