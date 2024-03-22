package com.easyflow.diarycourse.domain.models

import android.os.Parcelable
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Color
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Priority
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScheduleItem(
    val id: Int? = null,
    val text: String,
    val description: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val duration: String = "",
    val color: Color,
    var isCompleteTask: Boolean = false,
    val priority: Priority
) : Parcelable