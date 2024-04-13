package com.easyflow.diarycourse.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_category")
data class CategoryItemDto(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val name: String,
    val color: String
)
