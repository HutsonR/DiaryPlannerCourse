package com.easyflow.diarycourse.features.feature_home.adapter

import com.easyflow.diarycourse.domain.models.ScheduleItem

internal class TaskListUiConverter {

    fun convertToTaskListItem(tasks: List<ScheduleItem>): List<TaskListItem> {
        return mutableListOf<TaskListItem>().apply {
            addAll(tasks.map(::convertToScheduleItem))
//
//            if (isLoading) {
//                add(TaskListItem.Loading)
//            }
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
}