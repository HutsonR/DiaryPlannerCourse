package com.easyflow.diarycourse.features.feature_home.task.dialogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TaskColor
import javax.inject.Inject

class TaskDurationViewModel @Inject constructor() :
    BaseViewModel<TaskDurationViewModel.State, TaskDurationViewModel.Actions>(State()) {

    fun updateTask(item: ScheduleItem) {
        val updatedDuration = if (item.isAllDay) "" else calculateTaskDuration(item.startTime, item.endTime)
        val updatedTask = item.copy(duration = updatedDuration)
        modifyState {
            copy(
                task = updatedTask,
                taskColor = updatedTask.taskColor
            )
        }
        updateSaveButtonState()
    }

    fun updateDate(date: String) {
        modifyState { copy(task = task?.copy(date = date)) }
        updateSaveButtonState()
    }

    fun saveTaskDuration() {
        getState().task?.let {
            onAction(Actions.GoBackWithItem(it))
        }
    }

    fun showAlert() = onAction(Actions.ShowAlert)

    fun goBack() = onAction(Actions.GoBack)

    private fun updateSaveButtonState() {
        val task = getState().task ?: return
        val isEnable = task.date.isNotBlank() && (task.startTime.isNotBlank() || task.isAllDay)
        modifyState { copy(isSaveButtonEnable = isEnable) }
    }

    private fun calculateTaskDuration(startTime: String, endTime: String): String {
        if (endTime.isEmpty()) {
            return "бессрочно"
        }

        val (startHours, startMinutes) = startTime.split(":").map { it.toInt() }
        val (endHours, endMinutes) = endTime.split(":").map { it.toInt() }

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

    data class State(
        var task: ScheduleItem? = null,
        var taskColor: TaskColor = TaskColor.BLUE,
        var isSaveButtonEnable: Boolean = false
    )

    sealed interface Actions {
        data object ShowAlert : Actions
        data object GoBack : Actions
        data class GoBackWithItem(val item: ScheduleItem) : Actions
    }

    class TaskDurationViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskDurationViewModel() as T
        }
    }
}