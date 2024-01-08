package com.example.diarycourse.data.mapper

import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.data.models.ScheduleItemDto

object ScheduleItemMapper {

    fun fromDto(scheduleItemDto: ScheduleItemDto): ScheduleItem {
        return ScheduleItem(
            id = scheduleItemDto.id,
            text = scheduleItemDto.text,
            description = scheduleItemDto.description,
            date = scheduleItemDto.date,
            startTime = scheduleItemDto.startTime,
            endTime = scheduleItemDto.endTime,
            duration = scheduleItemDto.duration,
            isCompleteTask = scheduleItemDto.isCompleteTask
        )
    }

    fun toDto(scheduleItem: ScheduleItem): ScheduleItemDto {
        return ScheduleItemDto(
            id = scheduleItem.id ?: 0,
            text = scheduleItem.text,
            description = scheduleItem.description,
            date = scheduleItem.date,
            startTime = scheduleItem.startTime,
            endTime = scheduleItem.endTime,
            duration = scheduleItem.duration ?: "",
            isCompleteTask = scheduleItem.isCompleteTask
        )
    }
}