package com.phoenix.kspt.ovt.models

import android.os.Parcel
import android.os.Parcelable
import com.phoenix.kspt.models.ParentItem

/**
 * Created by darkt on 3/26/2018.
 * Stage it is a task in theme.
 */
class Stage() : ParentItem() {
    var stageCompleted: Boolean = false                     // for shows results
    var countAttempts: Int = 1
    var name: String = ""
    var userAnswers: ArrayList<CustomNumber> = ArrayList()  // item in stack answers

    constructor(parcel: Parcel) : this() {
        countAttempts = parcel.readInt()
        stageCompleted = parcel.readValue(Boolean::class.java.classLoader) as Boolean
        parcel.readList(userAnswers, CustomNumber::class.java.classLoader)
        name = parcel.readString()
    }

    constructor(userAnswers: ArrayList<CustomNumber>) : this() {
        this.userAnswers = userAnswers
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(countAttempts)
        dest.writeValue(stageCompleted)
        dest.writeList(userAnswers)
        dest.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Stage> {
        override fun createFromParcel(parcel: Parcel): Stage {
            return Stage(parcel)
        }

        override fun newArray(size: Int): Array<Stage?> {
            return arrayOfNulls(size)
        }
    }
}