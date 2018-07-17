package com.phoenix.kspt.ovt.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import com.phoenix.kspt.models.ParentItem

/**
 * Created by darkt on 9/23/2017.
 * All system var. It is pdf manuals and avatar links.
 */
@IgnoreExtraProperties
class SystemVars() : ParentItem(){

    var avatarDefault: String = ""
    var ovtTheme1URL: String = ""
    var ovtTheme2URL: String = ""
    var ovtTheme3URL: String = ""
    var ovtTheme4URL: String = ""
    var secretRegistrationWord: String = ""

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        avatarDefault = parcel.readString()
        ovtTheme1URL = parcel.readString()
        ovtTheme2URL = parcel.readString()
        ovtTheme3URL = parcel.readString()
        ovtTheme4URL = parcel.readString()
        secretRegistrationWord = parcel.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(avatarDefault)
        dest.writeString(ovtTheme1URL)
        dest.writeString(ovtTheme2URL)
        dest.writeString(ovtTheme3URL)
        dest.writeString(ovtTheme4URL)
        dest.writeString(secretRegistrationWord)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SystemVars> {
        override fun createFromParcel(parcel: Parcel): SystemVars {
            return SystemVars(parcel)
        }

        override fun newArray(size: Int): Array<SystemVars?> {
            return arrayOfNulls(size)
        }
    }
}