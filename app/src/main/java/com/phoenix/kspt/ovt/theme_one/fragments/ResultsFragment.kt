package com.phoenix.kspt.ovt.theme_one.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.phoenix.kspt.R
import com.phoenix.kspt.ovt.models.Stage
import com.phoenix.kspt.ovt.models.StudentResults
import com.phoenix.kspt.ovt.theme_one.adapters.ResultsAdapter
import com.phoenix.kspt.utils.COUNT_STAGE
import com.phoenix.kspt.utils.STUDENT_RESULT
import kotlinx.android.synthetic.main.ovt_1_fragment_result.*


class ResultsFragment : NumbersFragment() {
    private lateinit var adapter: ResultsAdapter
    private var stages: ArrayList<Stage> = ArrayList()
    private lateinit var studentResult: StudentResults
    var percent: Int = 0

    companion object {
        fun newInstance(studentResult: StudentResults): ResultsFragment {
            val fragment = ResultsFragment()
            val args = Bundle()
            args.putParcelable(STUDENT_RESULT, studentResult)
            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.ovt_1_fragment_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null) {
            studentResult = arguments!!.getParcelable(STUDENT_RESULT)
            updateResultPage(studentResult)
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateResultPage(studentResult: StudentResults) {
        stages.clear()
        adapter = ResultsAdapter(context!!, stages)
        recyclerView.adapter = adapter

        adapter.addItem(studentResult.stages[0]!!, getString(R.string.transformation_a))
        adapter.addItem(studentResult.stages[1]!!, getString(R.string.transformation_b))
        adapter.addItem(studentResult.stages[2]!!, getString(R.string.a_plus_b_result_additional))
        adapter.addItem(studentResult.stages[3]!!, getString(R.string.a_minus_b_result_inversion))
        adapter.addItem(studentResult.stages[4]!!, getString(R.string.b_minus_a_result_inversion))
        adapter.addItem(studentResult.stages[5]!!, getString(R.string.a_minus_b_result_additional))
        adapter.addItem(studentResult.stages[6]!!, getString(R.string.b_minus_a_result_additional))

        var markInt = 0
        for (stage in studentResult.stages)
            if (stage.value.stageCompleted)
                markInt++

        percent = markInt * 100 / (COUNT_STAGE - 1)
        mark.text = "$percent%"
    }

    override fun getTitle(): String {
        return "Результаты"
    }

}
