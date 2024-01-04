package com.example.diarycourse.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_items")
data class ScheduleItemDto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: String,
    val endTime: String?,
    val text: String,
    val description: String,
    val duration: String,
    var isCompleteTask: Boolean = false
)