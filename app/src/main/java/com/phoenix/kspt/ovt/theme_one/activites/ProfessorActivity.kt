package com.phoenix.kspt.ovt.theme_one.activites

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.Toast
import com.gdacciaro.iOSDialog.iOSDialogBuilder
import com.phoenix.kspt.Application
import com.phoenix.kspt.Application.Companion.firebase
import com.phoenix.kspt.BuildConfig
import com.phoenix.kspt.R
import com.phoenix.kspt.utils.*
import com.phoenix.kspt.firebase.FirebaseFragment
import com.phoenix.kspt.ovt.models.StudentResults
import com.phoenix.kspt.ovt.theme_one.fragments.AssociationFragment
import com.phoenix.kspt.ovt.theme_one.fragments.FragmentPairTypes
import com.phoenix.kspt.ovt.theme_one.fragments.NumbersFragment
import kotlinx.android.synthetic.main.ovt_1_activity_professor.*

/**
 * Professor theme # 1 activity
 */
class ProfessorActivity : AppCompatActivity() {
    // systems
    private lateinit var pagerAdapter: ScreenSlidePagerAdapter
    private lateinit var currentFragment: FirebaseFragment

    // users
    private var studentResults = StudentResults()
    private var currentStage = 1
    private var userId: String = ""

    companion object {
        fun startActivity(context: Context, studentResults: StudentResults, userId: String) {
            val intent = Intent(context, ProfessorActivity::class.java)
            intent.putExtra(STUDENT_RESULT, studentResults)
            intent.putExtra(USER_ID, userId)
            val options = ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, R.anim.fade_out)
            context.startActivity(intent, options.toBundle())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ovt_1_activity_professor)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        studentResults = intent.getParcelableExtra(STUDENT_RESULT)
        userId = intent.getStringExtra(USER_ID)

        // toolbar
        HelpFunctions.initToolbarSettings(this)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // init static val for fragments
        NumbersFragment.numberA = studentResults.number1
        NumbersFragment.numberB = studentResults.number2
        NumbersFragment.launchMode = PROFESSOR_MODE
        AssociationFragment.studentResults = studentResults

        // init viewpager
        val fragments: ArrayList<FirebaseFragment> = ArrayList()
        fragments.add(AssociationFragment.newInstance(FragmentPairTypes.CONVERSATION_A))
        fragments.add(AssociationFragment.newInstance(FragmentPairTypes.CONVERSATION_B))
        fragments.add(AssociationFragment.newInstance(FragmentPairTypes.AB_DIRECT))
        fragments.add(AssociationFragment.newInstance(FragmentPairTypes.AB_INVERSION))
        fragments.add(AssociationFragment.newInstance(FragmentPairTypes.BA_INVERSION))
        fragments.add(AssociationFragment.newInstance(FragmentPairTypes.AB_ADDITIONAL))
        fragments.add(AssociationFragment.newInstance(FragmentPairTypes.BA_ADDITIONAL))

        pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager, fragments)
        pager.adapter = pagerAdapter
        currentFragment = pagerAdapter.getItem(0)
        updateProgressBar()

        // listeners
        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                currentStage = position + 1
                currentFragment = pagerAdapter.getItem(position)
                updateProgressBar()

                // show errors in the student part
                if (!(currentFragment as AssociationFragment).fragment1!!.stage.stageCompleted) {
                    (currentFragment as AssociationFragment).fragment1!!.verificationOfAnswers(true)
                    stageStatus.background = ContextCompat.getDrawable(Application.mContext, R.drawable.ic_favorite_border_24dp)
                } else {
                    stageStatus.background = ContextCompat.getDrawable(Application.mContext, R.drawable.ic_favorite_24dp)
                }
            }
        })

        newControlButton.setOnClickListener {
            iOSDialogBuilder(this)
                    .setTitle(getString(R.string.are_you_sure_reset_result))
                    .setSubtitle(getString(R.string.All_data_will_be_lost))
                    .setBoldPositiveLabel(true)
                    .setCancelable(false)
                    .setPositiveListener(getString(R.string.Yes)) { dialog ->
                        if (BuildConfig.FLAVOR.equals(DEVELOP)) {
                            Toast.makeText(this, getString(R.string.Sorry_you_cant_reset_variant), Toast.LENGTH_LONG).show()
                        } else {
                            firebase.removeUserAnswersTaskOne(userId)
                            onBackPressed()
                            dialog.dismiss()
                        }
                    }
                    .setNegativeListener(getString(R.string.No)) { dialog -> dialog.dismiss() }
                    .build().show()

        }
    }

    override fun onBackPressed() {
        overridePendingTransition(R.anim.left_animation_leave, R.anim.right_animation_enter)
        super.onBackPressed()
    }

    private fun updateProgressBar() {
        supportActionBar!!.title = getString(R.string.Step) + " $currentStage. ${currentFragment.getTitle()}"
        val progress = currentStage * 100 / (COUNT_STAGE - 1)
        progressBar.progress = progress
    }
}
