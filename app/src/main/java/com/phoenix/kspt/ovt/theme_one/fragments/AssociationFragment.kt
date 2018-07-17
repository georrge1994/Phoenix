package com.phoenix.kspt.ovt.theme_one.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.phoenix.kspt.Application.Companion.mContext
import com.phoenix.kspt.R
import com.phoenix.kspt.utils.FRAGMENT_PAIR_TYPES
import com.phoenix.kspt.firebase.FirebaseFragment
import com.phoenix.kspt.ovt.models.Stage
import com.phoenix.kspt.ovt.models.StudentResults
import com.phoenix.kspt.ovt.theme_one.BinaryFunctions
import com.phoenix.kspt.ovt.theme_one.BinaryFunctionsInterface.OperationsCode

enum class FragmentPairTypes { CONVERSATION_A, CONVERSATION_B, AB_DIRECT, AB_INVERSION,
    BA_INVERSION, AB_ADDITIONAL, BA_ADDITIONAL
}

class AssociationFragment : FirebaseFragment() {
    var fragment1: NumbersFragment? = null
    private var fragment2: NumbersFragment? = null
    private lateinit var pairType: FragmentPairTypes

    companion object {
        var studentResults = StudentResults()

        @JvmStatic
        fun newInstance(pairType: FragmentPairTypes): AssociationFragment {
            val fragment = AssociationFragment()
            val bundle = Bundle()
            bundle.putSerializable(FRAGMENT_PAIR_TYPES, pairType)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_association, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments != null)
            pairType = arguments!!.get(FRAGMENT_PAIR_TYPES) as FragmentPairTypes

        when (pairType) {
            FragmentPairTypes.CONVERSATION_A -> replaceConversation(NumbersFragment.numberA, getString(R.string.letter_a), 0)
            FragmentPairTypes.CONVERSATION_B -> replaceConversation(NumbersFragment.numberB, getString(R.string.letter_b), 1)
            FragmentPairTypes.AB_DIRECT -> replaceSummation(OperationsCode.A_PLUS_B, 2)
            FragmentPairTypes.AB_INVERSION -> replaceSummation(OperationsCode.A_MINUS_B_INVERSION, 3)
            FragmentPairTypes.BA_INVERSION -> replaceSummation(OperationsCode.B_MINUS_A_INVERSION, 4)
            FragmentPairTypes.AB_ADDITIONAL -> replaceSummation(OperationsCode.A_MINUS_B_ADDITIONAL, 5)
            FragmentPairTypes.BA_ADDITIONAL -> replaceSummation(OperationsCode.B_MINUS_A_ADDITIONAL, 6)
        }
    }

    private fun replaceConversation(number: Double, letter: String, position: Int) {
        fragment1 = ConversationFragment.newInstance(number, letter, studentResults.stages[position])
        fragment2 = ConversationFragment.newInstance(number, letter, null)
        replace(fragment1!!, fragment2!!)
    }

    private fun replaceSummation(operationsCode: OperationsCode, position: Int) {
        fragment1 = SumFragment.newInstance(operationsCode, studentResults.stages[position])
        fragment2 = SumFragment.newInstance(operationsCode, null)
        replace(fragment1!!, fragment2!!)
    }

    private fun replace(fragment1: NumbersFragment, fragment2: NumbersFragment) {
        childFragmentManager.beginTransaction()
                .replace(R.id.container1, fragment1, fragment1.tag)
                .replace(R.id.container2, fragment2, fragment2.tag)
                .commit()
    }

    override fun getTitle(): String {
        return if (fragment1 != null)
            fragment1!!.getTitle()
        else
            mContext.getString(R.string.Results)
    }
}
