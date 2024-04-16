package com.easyflow.diarycourse.collapsiblecalendar.data;

import android.os.Parcel
import android.os.Parcelable
import java.util.Calendar

data class Day(val year: Int, val month: Int, val day: Int) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readInt(), parcel.readInt(), parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(year)
        parcel.writeInt(month)
        parcel.writeInt(day)
    }

    override fun describeContents() = 0

    private fun toUnixTime(): Long = Calendar.getInstance().run {
        set(year, month, day)
        timeInMillis
    }

    fun getDiff(): Int = ((toUnixTime() - today().toUnixTime()) / (1000 * 60 * 60 * 24)).toInt()

    companion object CREATOR : Parcelable.Creator<Day> {
        override fun createFromParcel(parcel: Parcel) = Day(parcel)
        override fun newArray(size: Int) = arrayOfNulls<Day?>(size)

        fun today() = Calendar.getInstance().run {
            Day(get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))
        }
    }
}

