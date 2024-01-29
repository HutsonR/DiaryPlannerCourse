package com.example.diarycourse.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_items")
data class ScheduleItemDto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val description: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val duration: String,
    val color: String,
    var isCompleteTask: Boolean = false,
    val priority: String
)