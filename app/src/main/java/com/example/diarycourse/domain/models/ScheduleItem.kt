package com.example.diarycourse.domain.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ScheduleItem(
    val id: Int? = null,
    val text: String,
    val description: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val duration: String,
    var isCompleteTask: Boolean = false
) : Parcelable