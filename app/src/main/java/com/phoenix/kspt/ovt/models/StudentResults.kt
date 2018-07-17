package com.phoenix.kspt.ovt.models

import android.os.Parcel
import android.os.Parcelable
import com.phoenix.kspt.utils.COUNT_STAGE
import com.phoenix.kspt.models.ParentItem

/**
 * Created by darkt on 3/26/2018.
 * All student's answers for theme # 1
 */
class StudentResults : ParentItem {
    var number1: Double = 0.0
    var number2: Double = 0.0
    var percent: Int = 0
    var stages: HashMap<Int, Stage> = HashMap()

    constructor() : super() {
        if (stages.size == 0)
            addEmptyStages()
    }

    constructor(parcel: Parcel) : this() {
        number1 = parcel.readDouble()
        number2 = parcel.readDouble()
        percent = parcel.readInt()

        // read HashMap
        val stagesSize = parcel.readInt()
        this.stages = HashMap(stagesSize)
        if (stagesSize > 0) {
            for (i in 0 until stagesSize) {
                val key = parcel.readInt()
                val value = parcel.readParcelable(Stage::class.java.classLoader) as Stage
                this.stages[key] = value
            }
        }
    }

    /**
     * We should to show the empty fields if student doesn't have answers
     */
    private fun addEmptyStages() {
        for (index in 0 until COUNT_STAGE)
            stages[index] = Stage()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeDouble(number1)
        dest.writeDouble(number2)
        dest.writeInt(percent)

        dest.writeInt(stages.size)
        for (entry in stages) {
            dest.writeInt(entry.key)
            dest.writeParcelable(entry.value, flags)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StudentResults> {
        override fun createFromParcel(parcel: Parcel): StudentResults {
            return StudentResults(parcel)
        }

        override fun newArray(size: Int): Array<StudentResults?> {
            return arrayOfNulls(size)
        }
    }
}