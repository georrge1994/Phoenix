package com.phoenix.kspt.people

/**
 * Created by darkt on 1/9/2018.
 */

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.phoenix.kspt.models.User

/**
 * Callback listener for opening user profile by click.
 */
interface ClickToProfile {
    /**
     * Open user's profile
     */
    fun openProfile(user: User)
}

/**
 * @items - list of the users
 * @clickToProfile - callback listener
 */
class PeopleAdapter(context: Context, private var items: MutableList<User>, private var clickToProfile: ClickToProfile) :
        RecyclerView.Adapter<PeopleViewHolderItem>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onBindViewHolder(viewHolder: PeopleViewHolderItem, position: Int) {
        viewHolder.bindTo(items[position])
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PeopleViewHolderItem {
        return PeopleViewHolderItem.create(inflater, viewGroup, clickToProfile)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}