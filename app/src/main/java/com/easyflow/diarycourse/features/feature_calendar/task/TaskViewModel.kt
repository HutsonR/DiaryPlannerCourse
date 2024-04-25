package com.easyflow.diarycourse.features.feature_calendar.task

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Priority
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TaskColor
import com.easyflow.diarycourse.features.feature_calendar.task.util.TaskType
import javax.inject.Inject

class TaskViewModel @Inject constructor() : BaseViewModel<TaskViewModel.State, TaskViewModel.Actions>(TaskViewModel.State()) {
    private var currentTask: ScheduleItem? = null
    private var parcelTask: ScheduleItem? = null
    private var type: TaskType = TaskType.ADD

    fun setParcelItem(item: ScheduleItem) {
        parcelTask = item
        currentTask = item
        type = TaskType.CHANGE

        Log.d("debugTag", "updateSaveButtonState FROM VM setParcelItem")
        updateSaveButtonState()
    }

    fun updateTask(item: ScheduleItem) {
        if (type == TaskType.CHANGE) {
            parcelTask = item
        } else {
            currentTask = item
        }

        if (type == TaskType.CHANGE) {
            modifyState { copy(item = parcelTask) }
        } else {
            modifyState { copy(item = currentTask) }
        }

        Log.d("debugTag", "updateSaveButtonState FROM VM updateTask")
        updateSaveButtonState()
    }

    fun clearTime() {
        if (type == TaskType.CHANGE) {
            parcelTask?.let {
                updateTask(it.copy(
                    startTime = "",
                    endTime = ""
                ))
            }
        } else {
            currentTask?.let {
                updateTask(it.copy(
                    startTime = "",
                    endTime = ""
                ))
            }
        }
        Log.d("debugTag", "updateSaveButtonState FROM VM clearTime")
        updateSaveButtonState()
    }

    fun reminderOpen(isChecked: Boolean) {
        Log.d("debugTag", "reminderOpen")
        onAction(Actions.OpenReminder(isChecked))
    }

    fun onSaveButtonClicked() {
        var item: ScheduleItem? = null
        if (type == TaskType.CHANGE) {
            parcelTask?.let {
                item = it.copy(
                    text = it.text,
                    description = it.description,
                    date = it.date,
                    priority = it.priority,
                    startTime = it.startTime,
                    endTime = it.endTime,
                    duration = calculateDuration(it.startTime, it.endTime),
                    taskColor = it.taskColor,
                    isCompleteTask = it.isCompleteTask
                )
            }
        } else {
            currentTask?.let {
                item = it.copy(
                    text = it.text,
                    description = it.description,
                    date = it.date,
                    priority = it.priority,
                    startTime = it.startTime,
                    endTime = it.endTime,
                    duration = calculateDuration(it.startTime, it.endTime),
                    taskColor = it.taskColor,
                    isCompleteTask = it.isCompleteTask
                )
            }
        }
        item?.let { onAction(Actions.GoBackWithItem(it)) }
    }

    private fun calculateDuration(startTime: String, endTime: String): String {
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

//    States
    fun updateReminderState() {
        Log.d("debugTag", "updateReminderState")
        currentTask?.let { task ->
            Log.d("debugTag", "updateReminderState task not null")
            if (task.date.isNotEmpty()) {
                onAction(Actions.ChangeReminderState(true))
            }
        }
    }

    fun updateSaveButtonState() {
        Log.d("debugTag", "updateSaveButtonState currentTask $currentTask")
        Log.d("debugTag", "updateSaveButtonState parcelTask $parcelTask")
        if (type == TaskType.CHANGE) {
            val isEnabled = currentTask != parcelTask
            onAction(Actions.ChangeSaveButtonState(isEnabled))
        } else {
            var isEnabled = false
            currentTask?.let {
                val isTitleFilled = it.text.isNotEmpty()
                val isDateFilled = it.date.isNotEmpty()
                val isTimeStartFilled = it.startTime.isNotEmpty()

                isEnabled = isTitleFilled && isDateFilled && isTimeStartFilled
            }
            onAction(Actions.ChangeSaveButtonState(isEnabled))
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

    fun getColorEnum(colorString: String): TaskColor {
        return try {
            TaskColor.valueOf(colorString)
        } catch (e: IllegalArgumentException) {
            TaskColor.BLUE
        }
    }

    fun goBack() {
        Log.d("debugTag", "goBack")
        onAction(Actions.GoBack)
    }

    data class State(
        var item: ScheduleItem? = null
    )

    sealed interface Actions {
        data object GoBack : Actions
        data class OpenReminder(val isChecked: Boolean) : Actions
        data class GoBackWithItem(val item: ScheduleItem) : Actions
        data class ChangeReminderState(val state: Boolean): Actions
        data class ChangeSaveButtonState(val state: Boolean): Actions
    }

    class TaskViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskViewModel() as T
        }
    }
}