package com.easyflow.diarycourse.features.feature_calendar.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Priority
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TaskColor
import javax.inject.Inject

class TaskViewModel @Inject constructor() : BaseViewModel<TaskViewModel.State, TaskViewModel.Actions>(TaskViewModel.State()) {
    private var title: String = ""
    private var text: String = ""
    private var date: String = ""
    private var priority: Priority = Priority.STANDARD
    private var timeStart: String = ""
    private var timeEnd: String = ""
    private var taskColor: TaskColor = TaskColor.BLUE
    private var parcelItem: ScheduleItem? = null

//    fun setParcelItem(item: ScheduleItem) {
//        title = item.text
//        text = item.description
//        date = item.date
//        priority = item.priority
//        timeStart = item.startTime
//        timeEnd = item.endTime
//        taskColor = item.taskColor
//
//        parcelItem = item
//    }
//
//    fun setReminder() {
//
//    }
//
//    fun updateSaveButtonState() {
//        if (parcelItem != null) {
//            parcelItem !=
//            // Для редактирования элемента
//            val isTitleChanged = title != previousTitle
//            val isDateChanged = date != previousDate
//            val isTimeStartChanged = timeStart != previousTimeStart
//            val isTextChanged = text != previousText
//            val isTimeEndChanged = timeEnd != previousTimeEnd
//            val isColorChanged = taskColor != previousTaskColor
//            val isPriorityChanged = priority != previousPriority
//
//            val isEnabled =
//                (isTitleChanged || isDateChanged || isTimeStartChanged || isTextChanged || isTimeEndChanged || isColorChanged || isPriorityChanged) &&
//                        title.isNotEmpty() && date.isNotEmpty() && timeStart.isNotEmpty()
//
//            saveButton.isEnabled = isEnabled
//            saveButtonTV.alpha = if (isEnabled) 1.0f else 0.6f
//        } else {
//            // По умолчанию обычное добавление элемента
//            val isTitleFilled = title.isNotEmpty()
//            val isDateFilled = date.isNotEmpty()
//            val isTimeStartFilled = timeStart.isNotEmpty()
//
//            val isEnabled = isTitleFilled && isDateFilled && isTimeStartFilled
//
//            saveButton.isEnabled = isEnabled
//            saveButtonTV.alpha = if (isEnabled) 1.0f else 0.6f
//        }
//    }
//
//    fun onSaveButtonClicked() {
//        val item: ScheduleItem
//        if (parcelItem != null) {
//            // Для редактирования элемента
//            item = parcelItem!!.copy(
//                text = title,
//                description = text,
//                date = date,
//                priority = priority,
//                startTime = timeStart,
//                endTime = timeEnd,
//                duration = calculateDuration(timeStart, timeEnd),
//                taskColor = taskColor,
//                isCompleteTask = parcelItem!!.isCompleteTask
//            )
//        } else {
//            // По умолчанию обычное добавление элемента
//            item = ScheduleItem(
//                text = title,
//                description = text,
//                date = date,
//                priority = priority,
//                startTime = timeStart,
//                endTime = timeEnd,
//                duration = calculateDuration(timeStart, timeEnd),
//                taskColor = taskColor,
//                isCompleteTask = false
//            )
//            setReminder()
//        }
//        onAction(Actions.GoBackWithItem(item))
//    }

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

    fun getColorEnum(colorString: String): TaskColor {
        return try {
            TaskColor.valueOf(colorString)
        } catch (e: IllegalArgumentException) {
            TaskColor.BLUE
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
//        data class GoBackWithItem(val item: ScheduleItem) : Actions
    }

    class TaskViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskViewModel() as T
        }
    }
}