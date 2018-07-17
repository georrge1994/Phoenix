package com.phoenix.kspt.ovt.home

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.phoenix.kspt.ovt.models.Theme

/**
 * Adapter for home screen of the course ovt
 */
class OvtHomeAdapter(context: Context, private var userId: String, private var items: ArrayList<Theme>,
                     private val listener: HomeOvtListeners) :
        RecyclerView.Adapter<OvtHomeViewHolderItem>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onBindViewHolder(holder: OvtHomeViewHolderItem, position: Int) {
        holder.bindTo(items[position], userId)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): OvtHomeViewHolderItem {
        return OvtHomeViewHolderItem.create(inflater, viewGroup, listener)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    /**
     * Callback listener for Home OVT fragment
     */
    interface HomeOvtListeners {
        /**
         * Open a pdf document by url
         */
        fun openPdf(url: String)

        /**
         * Open theme's activity in control mode
         */
        fun startControlWork(position: Int)

        /**
         * Open theme's activity in preparation mode
         */
        fun startPreparation(position: Int)

        /**
         * Refresh recyclerView after selecting element
         */
        fun refreshRecyclerView()
    }
}
