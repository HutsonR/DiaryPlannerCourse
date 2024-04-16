package com.easyflow.diarycourse.data.mapper

import com.easyflow.diarycourse.data.models.NoteItemDto
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.data.models.ScheduleItemDto
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.TaskColor
import com.easyflow.diarycourse.features.feature_calendar.schedule.utils.Priority

object Mapper {

    fun mapTo(noteItemDto: NoteItemDto): NoteItem {
        return NoteItem(
            id = noteItemDto.id,
            text = noteItemDto.text,
            date = noteItemDto.date
        )
    }
    fun mapFrom(noteItem: NoteItem): NoteItemDto {
        return NoteItemDto(
            id = noteItem.id ?: 0,
            text = noteItem.text,
            date = noteItem.date,
        )
    }

    fun mapTo(scheduleItemDto: ScheduleItemDto): ScheduleItem {
        return ScheduleItem(
            id = scheduleItemDto.id,
            text = scheduleItemDto.text,
            description = scheduleItemDto.description,
            date = scheduleItemDto.date,
            startTime = scheduleItemDto.startTime,
            endTime = scheduleItemDto.endTime,
            duration = scheduleItemDto.duration,
            taskColor = getColorEnum(scheduleItemDto.color),
            isCompleteTask = scheduleItemDto.isCompleteTask,
            priority = getPriorityEnum(scheduleItemDto.priority)
        )
    }
    fun mapFrom(scheduleItem: ScheduleItem): ScheduleItemDto {
        return ScheduleItemDto(
            id = scheduleItem.id ?: 0,
            text = scheduleItem.text,
            description = scheduleItem.description,
            date = scheduleItem.date,
            startTime = scheduleItem.startTime,
            endTime = scheduleItem.endTime,
            duration = scheduleItem.duration,
            color = scheduleItem.taskColor.name,
            isCompleteTask = scheduleItem.isCompleteTask,
            priority = scheduleItem.priority.name
        )
    }

    private fun getColorEnum(colorString: String): TaskColor {
        return try {
            TaskColor.valueOf(colorString)
        } catch (e: IllegalArgumentException) {
            TaskColor.BLUE
        }
    }

    private fun getPriorityEnum(priorityString: String): Priority {
        return try {
            Priority.valueOf(priorityString)
        } catch (e: IllegalArgumentException) {
            Priority.STANDARD
        }
    }
}