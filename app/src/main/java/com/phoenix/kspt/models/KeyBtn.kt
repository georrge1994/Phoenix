package com.phoenix.kspt.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

/**
 * Created by darkt on 9/23/2017.
 * Model of the button in custom keyboard
 */
@IgnoreExtraProperties
class KeyBtn() : ParentItem() {

    var text: String = "0"          // view text
    var data: String = "0"          // data which will be setting after click to button
    var typeButton: String = REINIT // type of the button

    constructor(text: String, data: String, type: String) : this() {
        this.text = text
        this.data = data
        this.typeButton = type
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        text = parcel.readString()
        data = parcel.readString()
        typeButton = parcel.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(text)
        dest.writeString(data)
        dest.writeString(typeButton)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<KeyBtn> {
        const val REINIT: String = "reinit"         // remove old content in EditText and insert keyBtn's @data
        const val ADD: String = "add"               // add keyBtn's @data to existing content
        const val BACKSPACE: String = "backspace"   // remove one symbol from right side
        const val DELETE: String = "delete"         // remove one symbol from left side

        override fun createFromParcel(parcel: Parcel): KeyBtn {
            return KeyBtn(parcel)
        }

        override fun newArray(size: Int): Array<KeyBtn?> {
            return arrayOfNulls(size)
        }
    }
}