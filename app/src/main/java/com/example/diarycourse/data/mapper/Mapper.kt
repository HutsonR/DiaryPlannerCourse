package com.example.diarycourse.data.mapper

import com.example.diarycourse.data.models.NoteItemDto
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.data.models.ScheduleItemDto
import com.example.diarycourse.domain.models.NoteItem
import com.example.diarycourse.features.feature_home.schedule.utils.Color

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
            color = getColorEnum(scheduleItemDto.color),
            isCompleteTask = scheduleItemDto.isCompleteTask
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
            color = scheduleItem.color.name,
            isCompleteTask = scheduleItem.isCompleteTask
        )
    }


    private fun getColorEnum(colorString: String): Color {
        return try {
            Color.valueOf(colorString)
        } catch (e: IllegalArgumentException) {
            Color.BLUE
        }
    }
}