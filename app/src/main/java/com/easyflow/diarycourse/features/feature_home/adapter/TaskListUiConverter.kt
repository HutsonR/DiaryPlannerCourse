package com.easyflow.diarycourse.features.feature_home.adapter

import com.easyflow.diarycourse.domain.models.ScheduleItem

internal class TaskListUiConverter {

    fun convertToTaskListItem(tasks: List<ScheduleItem>, isLoading: Boolean): List<TaskListItem> {
        return mutableListOf<TaskListItem>().apply {
            var currentDate = ""
            for (task in tasks) {
                if(task.date.isNotBlank()) {
                    val date = convertDate(task.date)
                    if (currentDate != date) {
                        currentDate = date
                        add(TaskListItem.DateHeader(date))
                    }
                }
                add(convertToScheduleItem(task))
            }

            if (isLoading) {
                add(TaskListItem.Loading)
            }
        }
    }

    private fun convertToScheduleItem(task: ScheduleItem): TaskListItem =
        TaskListItem.Task(
            id = (task.id ?: "0").toString(),
            text = task.text,
            description = task.description,
            date = task.date,
            startTime = task.startTime,
            endTime = task.endTime,
            taskColor = task.taskColor,
            duration = task.duration,
            isCompleteTask = task.isCompleteTask,
            priority = task.priority
        )

    private fun convertDate(date: String): String {
        val dateParts = date.split(".")
        return "${dateParts[0]} ${monthToString(dateParts[1].toInt())}"
    }

    private fun monthToString(month: Int): String {
        return when (month) {
            1 -> "Января"
            2 -> "Февраля"
            3 -> "Марта"
            4 -> "Апреля"
            5 -> "Мая"
            6 -> "Июня"
            7 -> "Июля"
            8 -> "Августа"
            9 -> "Сентября"
            10 -> "Октября"
            11 -> "Ноября"
            12 -> "Декабря"
            else -> ""
        }
    }
}