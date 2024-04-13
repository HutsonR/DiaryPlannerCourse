package com.easyflow.diarycourse.features.feature_calendar.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Color
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Priority
import javax.inject.Inject

class TaskViewModel @Inject constructor() : BaseViewModel<TaskViewModel.State, TaskViewModel.Actions>(TaskViewModel.State()) {

    fun calculateDuration(startTime: String, endTime: String): String {
        if (endTime.isEmpty()) {
            return "бессрочно"
        }

        val startParts = startTime.split(":")
        val endParts = endTime.split(":")

        val startHours = startParts[0].toInt()
        val startMinutes = startParts[1].toInt()

        val endHours = endParts[0].toInt()
        val endMinutes = endParts[1].toInt()

        val durationMinutes = (endHours * 60 + endMinutes) - (startHours * 60 + startMinutes)

        val durationHours = durationMinutes / 60
        val remainingMinutes = durationMinutes % 60

        return when {
            durationHours > 0 && remainingMinutes > 0 -> "$durationHours ч. $remainingMinutes мин."
            durationHours > 0 -> "$durationHours ч."
            remainingMinutes > 0 -> "$remainingMinutes мин."
            else -> "0 мин."
        }
    }

//    Вспомогательные функции для форматирования данных или конвертации

    fun getPriorityString(priority: Priority): String {
        return when (priority) {
            Priority.STANDARD -> "Обычный приоритет"
            Priority.IMPORTANT -> "Высокий приоритет"
        }
    }

    fun getPriorityEnum(priorityString: String): Priority {
        return when (priorityString) {
            "Обычный приоритет" -> Priority.STANDARD
            "Высокий приоритет" -> Priority.IMPORTANT
            else -> Priority.STANDARD
        }
    }

    fun getColorEnum(colorString: String): Color {
        return try {
            Color.valueOf(colorString)
        } catch (e: IllegalArgumentException) {
            Color.BLUE
        }
    }

    fun goBack() {
        onAction(Actions.GoBack)
    }

    data class State(
        var update: Resource? = null
    )

    sealed interface Actions {
        data object GoBack : Actions
    }

    class TaskViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskViewModel() as T
        }
    }
}