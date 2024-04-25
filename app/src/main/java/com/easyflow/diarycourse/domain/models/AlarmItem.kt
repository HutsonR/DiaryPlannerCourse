package com.easyflow.diarycourse.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlarmItem(
    val alarmTime : Long,
    val message : String
) : Parcelable
