package com.example.diarycourse.data.mapper

import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.data.models.ScheduleItemDto

object ScheduleItemMapper {

    fun fromDto(scheduleItemDto: ScheduleItemDto): ScheduleItem {
        return ScheduleItem(
            id = scheduleItemDto.id,
            startTime = scheduleItemDto.startTime,
            endTime = scheduleItemDto.endTime,
            text = scheduleItemDto.text,
            description = scheduleItemDto.description,
            duration = scheduleItemDto.duration,
            isCompleteTask = scheduleItemDto.isCompleteTask
        )
    }

    fun toDto(scheduleItem: ScheduleItem): ScheduleItemDto {
        return ScheduleItemDto(
            id = scheduleItem.id ?: 0,
            startTime = scheduleItem.startTime,
            endTime = scheduleItem.endTime,
            text = scheduleItem.text,
            description = scheduleItem.description,
            duration = scheduleItem.duration,
            isCompleteTask = scheduleItem.isCompleteTask
        )
    }
}