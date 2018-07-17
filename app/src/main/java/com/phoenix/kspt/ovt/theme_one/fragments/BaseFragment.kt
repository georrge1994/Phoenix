package com.phoenix.kspt.ovt.theme_one.fragments

import android.content.Context
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.View
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.phoenix.kspt.R
import com.phoenix.kspt.ovt.models.CustomNumber
import com.phoenix.kspt.ovt.theme_one.activites.FabMenuListener
import com.phoenix.kspt.utils.*
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem

open class BaseFragment : NumbersFragment() {
    private var mIsVisibleToUser: Boolean = false // you can see this variable may absolutely <=> getUserVisibleHint() but it not. Currently, after many test I find that
    protected var fabMenu: FloatingActionMenu? = null
    private lateinit var powerMenu: PowerMenu                      // popup menu
    private var fabMenuListener: FabMenuListener? = null
    protected var countBasicItems = 0

    /**
     * This method will call when viewpager create fragment and when we go to this fragment from
     * background or another activity, fragment
     * NOT call when we switch between each page in ViewPager
     */
    override fun onStart() {
        super.onStart()
        if (mIsVisibleToUser) {
            onVisible()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mIsVisibleToUser) {
            onInVisible()
        }
    }

    /**
     * This method will call at first time viewpager created and when we switch between each page
     * NOT called when we go to background or another activity (fragment) when we go back <-- bad comment
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        mIsVisibleToUser = isVisibleToUser
        if (isResumed) { // fragment have created
            if (mIsVisibleToUser) {
                onVisible()
            } else {
                onInVisible()
            }
        }
    }

    private fun onVisible() {
        if (NumbersFragment.launchMode != PROFESSOR_MODE) {
            fabMenu?.removeAllMenuButtons()
            initPopupTransformationMenu()
            addFabToMenu(getString(R.string.plus_field), R.drawable.fab_add, PLUS)
            addFabToMenu(getString(R.string.remove), R.drawable.ic_minus_24dp_white, MINUS)
            addFabToMenu(getString(R.string.transform), R.drawable.ic_compare_arrows_24dp, TRANSFORM)

            // if the last operation is conversation -> disable "+" button
            fabMenu?.getChildAt(0)?.isEnabled = recyclerViewAdapter?.getLast()?.typeOperation == PLUS
            // if operation stack contain only 3 operation -> disable "-" button
            fabMenu?.getChildAt(1)?.isEnabled = recyclerViewAdapter?.itemCount!! > countBasicItems
        } else {
            fabMenu?.visibility = View.GONE
        }
    }

    private fun onInVisible() {
        // nothing
    }

    private fun initPopupTransformationMenu() {
        powerMenu = PowerMenu.Builder(context)
                .addItem(PowerMenuItem(getString(R.string.transform_menu_1), true))
                .addItem(PowerMenuItem(getString(R.string.transform_menu_2), true))
                .addItem(PowerMenuItem(getString(R.string.transform_menu_3), true))
                .setAnimation(MenuAnimation.SHOWUP_TOP_LEFT) // Animation start point (TOP | LEFT)
                .setMenuRadius(10f)
                .setMenuShadow(10f)
                .setWidth(750) // set popup width size
                .setHeight(600) // set popup height size
                .setTextColor(ContextCompat.getColor(context!!, R.color.textColor))
                .setSelectedTextColor(Color.WHITE)
                .setMenuColor(Color.WHITE)
                .setSelectedMenuColor(ContextCompat.getColor(context!!, R.color.button_pressed))
                .setOnMenuItemClickListener(onMenuItemClickListener)
                .build()
    }

    private fun addFabToMenu(name: String, imageId: Int, typeOperation: String) {
        // Sign in
        val programFab = FloatingActionButton(context)
        programFab.labelText = name
        programFab.buttonSize = FloatingActionButton.SIZE_MINI
        programFab.colorNormal = ContextCompat.getColor(context!!, R.color.colorAccent)
        programFab.setImageResource(imageId)
        fabMenu?.addMenuButton(programFab)

        when (typeOperation) {
            PLUS -> programFab.setOnClickListener {
                recyclerViewAdapter?.addField(CustomNumber("", PLUS, ""))
                recyclerViewAdapter?.addField(CustomNumber("", PLUS, ""))
                fabMenu?.getChildAt(1)?.isEnabled = true
            }
            MINUS -> programFab.setOnClickListener {
                removeField()
                fabMenu?.getChildAt(1)?.isEnabled = recyclerViewAdapter?.itemCount!! > countBasicItems
            }
            TRANSFORM -> programFab.setOnClickListener {
                powerMenu.selectedPosition = -1
                powerMenu.showAsDropDown(activity?.window?.decorView,
                        activity?.window?.decorView!!.rootView.measuredWidth / 2 - powerMenu.contentViewWidth / 2,
                        -activity?.window?.decorView!!.rootView.measuredHeight / 2 - powerMenu.contentViewHeight / 2)
            }
            else -> {
            }
        }
    }

    private val onMenuItemClickListener = OnMenuItemClickListener<PowerMenuItem> { position, _ ->
        powerMenu.selectedPosition = position // change selected item

        when (position) {
            0 -> recyclerViewAdapter?.addField(CustomNumber("", FROM_INVERSION_TO_DIRECT, getString(R.string.direct)))
            1 -> recyclerViewAdapter?.addField(CustomNumber("", FROM_ADDITIONAL_TO_INVERSION, getString(R.string.inversion)))
            2 -> recyclerViewAdapter?.addField(CustomNumber("", FROM_DIRECT_TO_DECIMAL, getString(R.string.decimal)))
        }

        fabMenu?.getChildAt(0)?.isEnabled = false
        fabMenu?.getChildAt(1)?.isEnabled = true
        powerMenu.dismiss()
    }

    private fun removeField() {
        if (recyclerViewAdapter?.itemCount!! > countBasicItems){
            if(recyclerViewAdapter?.getLast()!!.isPlusOperation()) {
                recyclerViewAdapter?.removeLastField()
                recyclerViewAdapter?.removeLastField()
                // if the last field is conversation then plus button is disabled
                // else enabled
                fabMenu?.getChildAt(0)?.isEnabled = true
            } else {
                recyclerViewAdapter?.removeLastField()
                if(recyclerViewAdapter?.getLast()!!.isPlusOperation())
                    fabMenu?.getChildAt(0)?.isEnabled = true
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (fabMenuListener == null && context is FabMenuListener) {
            fabMenuListener = context
            fabMenu = fabMenuListener!!.getFabMenu()
        }
    }

    override fun onDetach() {
        super.onDetach()
        fabMenuListener = null
    }
}