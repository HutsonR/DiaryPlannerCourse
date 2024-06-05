package com.easyflow.diarycourse.features.feature_home.adapter

import com.easyflow.diarycourse.core.composite.CompositeItem
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Priority
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TaskColor

sealed class TaskListItem(override val id: String): CompositeItem {

    data class Task(
        override val id: String,
        val text: String,
        val description: String,
        val date: String,
        val startTime: String,
        val endTime: String,
        val duration: String = "",
        val taskColor: TaskColor = TaskColor.BLUE,
        var isCompleteTask: Boolean = false,
        val priority: Priority = Priority.STANDARD
    ): TaskListItem(id)

    data class DateHeader(
        val text: String,
    ): TaskListItem("DateHeader")

    data object Loading: TaskListItem("Loading")

}