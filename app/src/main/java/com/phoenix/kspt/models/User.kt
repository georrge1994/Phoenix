package com.phoenix.kspt.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import com.phoenix.kspt.utils.STUDENT
import kotlin.collections.HashMap

/**
 * Created by darkt on 9/23/2017.
 * User model
 */
@IgnoreExtraProperties
class User() : ParentItem() {

    var avatar: String? = null              // url
    var firstName: String = "user"
    var lastName: String = "user"
    var email: String = "email@email.com"
    var groupId: String = "-"               // student's group
    var userStatus: String = STUDENT        // status of the user (STUDENT or PROFESSOR)
    var percentOvt1: Int = 0                // patch. percent of correct answer for OVT.theme # 1
                                            // TODO: it should be replaced to student's result model

    constructor(firstName: String, lastName: String, email: String) : this() {
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        firstName = parcel.readString()
        lastName = parcel.readString()
        email = parcel.readString()
        groupId = parcel.readString()
        userStatus = parcel.readString()
        avatar = parcel.readString()
        percentOvt1 = parcel.readInt()
    }

    /**
     * Compressed object to map
     */
    @Exclude
    fun toMap(): Map<String, Any?> {
        val result = HashMap<String, Any?>()
        result["firstName"] = firstName
        result["lastName"] = lastName
        result["email"] = email
        result["groupId"] = groupId
        result["userStatus"] = userStatus
        result["avatar"] = avatar
        result["percentOvt1"] = percentOvt1
        return result
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(firstName)
        dest.writeString(lastName)
        dest.writeString(email)
        dest.writeString(groupId)
        dest.writeString(userStatus)
        dest.writeString(avatar)
        dest.writeInt(percentOvt1)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}