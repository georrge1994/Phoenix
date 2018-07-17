package com.phoenix.kspt.people

import android.annotation.SuppressLint
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.phoenix.kspt.R
import com.phoenix.kspt.models.User
import com.phoenix.kspt.utils.PROFESSOR
import kotlinx.android.synthetic.main.item_people.view.*

/**
 * Item for recycler view in people fragment
 */
class PeopleViewHolderItem private constructor(view: View,
                                               private val clickToProfile: ClickToProfile) : RecyclerView.ViewHolder(view) {
    private val itemHuman = view.itemPeople
    @SuppressLint("SetTextI18n")
    fun bindTo(user: User) {
        if (selectedPosition == layoutPosition) {
            itemHuman.setBackgroundColor(ContextCompat.getColor(itemHuman.context, R.color.blue))
            selectedPosition = -1
        } else
            itemHuman.setBackgroundColor(ContextCompat.getColor(itemHuman.context, R.color.white))

        itemHuman.avatar.setImageURI(user.avatar)
        itemHuman.nameField.text = user.firstName + " " + user.lastName

        if (user.userStatus == PROFESSOR) {
            itemHuman.groupIdField.text = itemHuman.context.getString(R.string.professor_group)
            itemHuman.percent.visibility = View.GONE
        } else {
            itemHuman.groupIdField.text = user.groupId
            itemHuman.percent.visibility = View.VISIBLE
        }
        itemHuman.percent.text = "${user.percentOvt1}%"

        // listener
        itemHuman.setOnClickListener { selectAndOpen(user) }
        itemHuman.moreBtn.setOnClickListener { selectAndOpen(user) }
    }

    private fun selectAndOpen(user: User) {
        clickToProfile.openProfile(user)
        selectedPosition = layoutPosition
    }

    companion object {
        var selectedPosition = -1
        fun create(inflater: LayoutInflater,
                   parent: ViewGroup, clickToProfile: ClickToProfile): PeopleViewHolderItem {
            return PeopleViewHolderItem(inflater.inflate(R.layout.item_people, parent, false), clickToProfile)
        }
    }
}