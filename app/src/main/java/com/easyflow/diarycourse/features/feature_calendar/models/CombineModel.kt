package com.easyflow.diarycourse.features.feature_calendar.models

import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TaskColor
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Priority

data class CombineModel(
    val id: Int? = null,
    val text: String,
    val description: String = "",
    val date: String,
    val startTime: String = "",
    val endTime: String = "",
    val duration: String = "",
    val taskColor: TaskColor = TaskColor.BLUE,
    var isCompleteTask: Boolean = false,
    val priority: Priority = Priority.STANDARD
)