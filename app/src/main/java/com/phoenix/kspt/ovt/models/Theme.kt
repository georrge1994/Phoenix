package com.phoenix.kspt.ovt.models

import android.os.Parcel
import android.os.Parcelable
import com.phoenix.kspt.models.ParentItem

/**
 * Created by darkt on 3/26/2018.
 * Theme's information model. Just information.
 */
class Theme() : ParentItem() {
    var pdfUrl: String = ""
    var number: Int = 1
    var name: String = ""
    var description: String = ""
    var enable: Boolean = true

    constructor(pdfUrl: String, name: String,
                description: String, enable: Boolean) : this() {
        this.pdfUrl = pdfUrl
        this.name = name
        this.description = description
        this.enable = enable
    }

    constructor(parcel: Parcel) : this() {
        pdfUrl = parcel.readString()
        name = parcel.readString()
        description = parcel.readString()
        enable = parcel.readValue(Boolean::class.java.classLoader) as Boolean
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(pdfUrl)
        dest.writeString(name)
        dest.writeString(description)
        dest.writeValue(enable)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Theme> {
        override fun createFromParcel(parcel: Parcel): Theme {
            return Theme(parcel)
        }

        override fun newArray(size: Int): Array<Theme?> {
            return arrayOfNulls(size)
        }
    }
}