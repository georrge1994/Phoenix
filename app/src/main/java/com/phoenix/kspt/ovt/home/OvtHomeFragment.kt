package com.phoenix.kspt.ovt.home


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.phoenix.kspt.Application.Companion.firebase
import com.phoenix.kspt.activites.MainWindowInterface
import com.phoenix.kspt.R
import com.phoenix.kspt.utils.*
import com.phoenix.kspt.ovt.models.Theme
import com.phoenix.kspt.ovt.models.StudentResults
import com.phoenix.kspt.ovt.home.OvtHomeAdapter.HomeOvtListeners
import com.phoenix.kspt.ovt.theme_one.activites.OvtCourseActivity
import com.phoenix.kspt.ovt.theme_one.activites.ProfessorActivity
import com.phoenix.kspt.activites.PdfBookDisplayActivity
import kotlinx.android.synthetic.main.ovt_fragment_home.*
import rx.Observable

/**
 * Home screen of the course OVT
 */
class OvtFragment : Fragment() {
    private lateinit var adapterRecycler: OvtHomeAdapter
    private var themes: ArrayList<Theme> = ArrayList()
    private var listener: MainWindowInterface? = null
    private var userId: String = ""

    companion object {
        fun newInstance(userId: String): OvtFragment {
            val fragment = OvtFragment()
            val bundle = Bundle()
            bundle.putString(USER_ID, userId)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ovt_fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()

        userId = arguments!!.getString(USER_ID)

        adapterRecycler = OvtHomeAdapter(context!!, userId, themes, object : HomeOvtListeners {
            override fun refreshRecyclerView() {
                adapterRecycler.notifyDataSetChanged()
            }

            override fun startControlWork(position: Int) {
                if (HelpFunctions.getCurrentUserStatus(context!!) == PROFESSOR)
                    runThemeActivity(position, PROFESSOR_MODE)
                else
                    runThemeActivity(position, CONTROL_MODE)
            }

            override fun startPreparation(position: Int) {
                runThemeActivity(position, PREPARE_MODE)
            }

            override fun openPdf(url: String) {
                PdfBookDisplayActivity.startActivity(context!!, url)
            }
        })

        recyclerView.adapter = adapterRecycler
        listener?.setTitle(getString(R.string.OVT))
    }

    private fun runThemeActivity(position: Int, launchMode: String) {
        when (position) {
            0 -> {
                if (launchMode == PREPARE_MODE)
                    OvtCourseActivity.startActivity(context!!, StudentResults(), launchMode, userId)
                else if (launchMode == CONTROL_MODE || launchMode == PROFESSOR_MODE) {
                    fetchAndLaunchThemeOne(launchMode)
                }
            }
            1, 2, 3 -> Toast.makeText(context, getString(R.string.This_part_in_develop), Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchAndLaunchThemeOne(launchMode: String) {
        Observable.zip(firebase.fetchStudentAnswersTaskOne(userId),
                firebase.fetchStages(userId)) { studentResult, stagesList ->
                    // init stages in studentResult object
                    for (i in 0 until stagesList.size)
                        studentResult.stages[i] = stagesList[i]
                    studentResult
                }.subscribe {
                    when {
                        launchMode == CONTROL_MODE && it != null -> {
                            OvtCourseActivity.startActivity(context!!, it, launchMode, userId)
                        }
                        launchMode == CONTROL_MODE && it == null -> {
                            // set new variant for student
                            val studentResult = StudentResults()
                            studentResult.number1 = HelpFunctions.getRandomNumber(NumberType.POSITIVE_DOUBLE)
                            studentResult.number2 = HelpFunctions.getRandomNumber(NumberType.POSITIVE_DOUBLE)
                            OvtCourseActivity.startActivity(context!!, studentResult, launchMode, userId)
                        }
                        launchMode == PROFESSOR_MODE && it != null -> ProfessorActivity.startActivity(context!!, it, userId)
                        launchMode == PROFESSOR_MODE && it == null ->
                            if (userId == HelpFunctions.getCurrentUserId(context!!)) {
                                // set new variant for student
                                val studentResult = StudentResults()
                                studentResult.number1 = HelpFunctions.getRandomNumber(NumberType.POSITIVE_DOUBLE)
                                studentResult.number2 = HelpFunctions.getRandomNumber(NumberType.POSITIVE_DOUBLE)
                                OvtCourseActivity.startActivity(context!!, studentResult, launchMode, userId)
                            } else
                                Toast.makeText(context, getString(R.string.Student_does_not_send_control_work), Toast.LENGTH_LONG).show()
                    }

                    // hide a dialog after receiving the response
                    listener?.visibilityWait(false)
                }
        listener?.visibilityWait(true)
    }

    private fun initList(){
        themes.add(Theme(HelpFunctions.getFileURL(context!!,OVT_THEME_1_URL), getString(R.string.ovtTheme1Name),
                getString(R.string.ovtTheme1Description), true))
        themes.add(Theme(HelpFunctions.getFileURL(context!!,OVT_THEME_2_URL), getString(R.string.ovtTheme2Name),
                getString(R.string.ovtTheme2Description), false))
        themes.add(Theme(HelpFunctions.getFileURL(context!!,OVT_THEME_3_URL), getString(R.string.ovtTheme3Name),
                getString(R.string.ovtTheme3Description), false))
        themes.add(Theme(HelpFunctions.getFileURL(context!!,OVT_THEME_4_URL), getString(R.string.ovtTheme4Name),
                getString(R.string.ovtTheme4Description), false))
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (listener == null && context is MainWindowInterface)
            listener = context
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
