package com.easyflow.diarycourse.domain.models

import android.os.Parcelable
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TaskColor
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoryItem(
    val id: Int? = null,
    val name: String,
    val taskColor: TaskColor
) : Parcelable
