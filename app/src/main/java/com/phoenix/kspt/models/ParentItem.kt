package com.phoenix.kspt.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Created by darkt on 1/16/2018.
 * It is parent class for anything model, which will be uploading to Firebase
 */
@IgnoreExtraProperties
open class ParentItem() : Parcelable {

    var id: String = ""

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.id)
    }

    protected constructor(parcel: Parcel) : this() {
        this.id = parcel.readString()
    }

    companion object CREATOR : Parcelable.Creator<ParentItem> {
        override fun createFromParcel(parcel: Parcel): ParentItem {
            return ParentItem(parcel)
        }

        override fun newArray(size: Int): Array<ParentItem?> {
            return arrayOfNulls(size)
        }
    }
}
