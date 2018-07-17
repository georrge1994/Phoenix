package com.phoenix.kspt.ovt.home

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.phoenix.kspt.R
import com.phoenix.kspt.ovt.models.Theme
import com.phoenix.kspt.utils.HelpFunctions
import com.phoenix.kspt.utils.PROFESSOR
import java.util.*
import kotlinx.android.synthetic.main.ovt_item_home.view.*

/**
 * Item for recycler view in home screen
 */
class OvtHomeViewHolderItem private constructor(private val view: View,
                                                val listener: OvtHomeAdapter.HomeOvtListeners) : RecyclerView.ViewHolder(view) {

    private val itemOvtHome = view.itemOvtHome

    companion object {
        private var selectedPosition: Int = -1

        fun create(inflater: LayoutInflater, parent: ViewGroup, listener: OvtHomeAdapter.HomeOvtListeners): OvtHomeViewHolderItem {
            return OvtHomeViewHolderItem(
                    inflater.inflate(R.layout.ovt_item_home, parent, false),
                    listener)
        }
    }

    @SuppressLint("SetTextI18n")
    fun bindTo(homeItem: Theme, userId: String) {

        visibilitySettings(userId, adapterPosition == selectedPosition) // close when it is not selected

        // set data
        itemOvtHome.numberField.text = (adapterPosition + 1).toString()
        itemOvtHome.name.text = homeItem.name
        val html = String.format(Locale.getDefault(), ("<html>\n" +         // it is for adjastence text
                "<body style=\"text-align:justify;color:gray;background-color:white;font-style: italic;\">" +
                homeItem.description +
                "</body>\n" +
                "</html>"))
        itemOvtHome.description.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)

        // init the listeners
        itemOvtHome.readBtn.setOnClickListener { listener.openPdf(homeItem.pdfUrl) }         // open a book
        itemOvtHome.moreBtn.setOnClickListener { refresh(adapterPosition) }                  // show details
        itemOvtHome.setOnClickListener { refresh(adapterPosition) }                          // show details

        if (!homeItem.enable) {
            itemOvtHome.controlBtn.setTextColor(ContextCompat.getColor(view.context, R.color.disableColor))
            itemOvtHome.preparationBtn.setTextColor(ContextCompat.getColor(view.context, R.color.disableColor))
            val bgShape = itemOvtHome.numberField.background as GradientDrawable
            bgShape.setColor(ContextCompat.getColor(view.context, R.color.disableColor))
        }

        itemOvtHome.controlBtn.setOnClickListener { listener.startControlWork(adapterPosition) }     // start control work
        itemOvtHome.preparationBtn.setOnClickListener { listener.startPreparation(adapterPosition) } // start preparation activity
    }

    private fun visibilitySettings(userId: String, open: Boolean) {
        itemOvtHome.moreBtn.visibility = if (open) View.INVISIBLE else View.VISIBLE
        itemOvtHome.description.visibility = if (open) View.VISIBLE else View.GONE
        itemOvtHome.readBtn.visibility = if (open) View.VISIBLE else View.GONE

        if(HelpFunctions.getCurrentUserStatus(itemOvtHome.context) == PROFESSOR){
            if (userId == HelpFunctions.getCurrentUserId(view.context)) {
                itemOvtHome.controlBtn.visibility = View.GONE               // professor can't to see the own control work
            } else {
                if(open)
                    itemOvtHome.controlBtn.visibility = View.VISIBLE        // he can see control btn only if click btn "more"
                else
                    itemOvtHome.controlBtn.visibility = View.GONE           // else he can't to watch it
            }
        } else {
            if (userId == HelpFunctions.getCurrentUserId(view.context)) {
                if(open)
                    itemOvtHome.controlBtn.visibility = View.VISIBLE        // user can see control own control work, if click btn "more"
                else
                    itemOvtHome.controlBtn.visibility = View.GONE           // else he can't to watch it
            } else {
                itemOvtHome.controlBtn.visibility = View.GONE               // also he can't watch control work other's students
            }
        }

        if (userId == HelpFunctions.getCurrentUserId(view.context) && open) {
            itemOvtHome.preparationBtn.visibility = View.VISIBLE
            if(HelpFunctions.getCurrentUserStatus(itemOvtHome.context) == PROFESSOR)
            itemOvtHome.controlBtn.visibility = View.GONE
        } else {
            itemOvtHome.preparationBtn.visibility = View.GONE
            if(open)
                itemOvtHome.controlBtn.visibility = View.VISIBLE
        }
        if (open)
            selectedPosition = adapterPosition
    }

    private fun refresh(position: Int) {
        selectedPosition = position
        listener.refreshRecyclerView()
    }
}