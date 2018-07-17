package com.phoenix.kspt.ovt.models

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude
import com.phoenix.kspt.utils.FROM_ADDITIONAL_TO_INVERSION
import com.phoenix.kspt.utils.FROM_DIRECT_TO_DECIMAL
import com.phoenix.kspt.utils.FROM_INVERSION_TO_DIRECT
import com.phoenix.kspt.utils.PLUS
import com.phoenix.kspt.models.ParentItem
import com.phoenix.kspt.ovt.theme_one.BinaryFunctions
import com.phoenix.kspt.ovt.theme_one.BinaryFunctions.Companion.BITSET_OVERFLOW_BIT
import com.phoenix.kspt.ovt.theme_one.BinaryFunctions.Companion.BITSET_SIGN_BIT
import java.util.*

/**
 * Created by darkt on 3/26/2018.
 */
class CustomNumber() : ParentItem() {
    // number's parameters
    var numberSet: BitSet = BitSet()            // number format's # 1 (for algorithms)

    var numberString: String = ""               // number's format # 2 (for EditText)
        get() = if (field.isNotEmpty())
            field
        else
            BinaryFunctions.getNumberInString(this)

    var comment: String = ""


    // field's parameters
    var typeOperation: String = PLUS

    var sign: Boolean = numberSet[BITSET_SIGN_BIT]
        set(value) {
            numberSet[BITSET_SIGN_BIT] = value
        }

    var startPositionInEditText = 0

    constructor(parcel: Parcel) : this() {
        numberString = parcel.readString()
        comment = parcel.readString()
        typeOperation = parcel.readString()
        numberSet = BinaryFunctions.getNumberFromString(numberString)
    }

    constructor(numberSet: BitSet) : this() {
        this.numberSet = numberSet
    }

    constructor(numberString: String, typeOperation: String, comment: String) : this() {
        this.numberString = numberString
        this.typeOperation = typeOperation
        this.comment = comment
    }

    constructor(numberString: String) : this() {
        numberSet = BinaryFunctions.getNumberFromString(numberString)
    }

    /**
     * height bit is 1, so negative (for algorithms)
     */
    fun isNegative(): Boolean {
        return numberSet[BITSET_SIGN_BIT]
    }

    /**
     * Return true if operation is transformation (for algorithms)
     */
    fun isTransformOperation(): Boolean {
        return typeOperation == FROM_INVERSION_TO_DIRECT ||
                typeOperation == FROM_ADDITIONAL_TO_INVERSION ||
                typeOperation == FROM_DIRECT_TO_DECIMAL
    }

    /**
     * Return true if operation is positive (for algorithms)
     */
    fun isPlusOperation(): Boolean {
        return typeOperation == PLUS
    }

    /**
     * Cloning object (for algorithms)
     */
    fun clone(): CustomNumber {
        val clone = CustomNumber()

        val numberSetClone = BitSet()
        for (i in 0 until numberSet.size())
            numberSetClone[i] = numberSet[i]
        clone.numberSet = numberSetClone

        clone.comment = comment
        clone.numberString = numberString

        return clone
    }

    /**
     * Compare objects (for algorithms)
     */
    override fun equals(other: Any?): Boolean {
        val numberSetOther = (other as CustomNumber).numberSet

        if (numberSet.size() != numberSetOther.size())
            return false

        for (i in 0 until numberSet.size())
            if (numberSet.get(i) != numberSetOther[i])
                return false

        return true
    }

    /**
     * Check that number(field) is empty(for algorithms)
     */
    fun isEmpty(): Boolean {
        return numberSet.isEmpty
    }

    /**
     * Mapping for firebase
     */
    @Exclude
    fun toMap(): Map<String, Any?> {
        val result = HashMap<String, Any?>()
        result["numberString"] = numberString
        result["comment"] = comment
        result["typeOperation"] = typeOperation

        return result
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(numberString)
        dest.writeString(comment)
        dest.writeString(typeOperation)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CustomNumber> {
        override fun createFromParcel(parcel: Parcel): CustomNumber {
            return CustomNumber(parcel)
        }

        override fun newArray(size: Int): Array<CustomNumber?> {
            return arrayOfNulls(size)
        }
    }
}