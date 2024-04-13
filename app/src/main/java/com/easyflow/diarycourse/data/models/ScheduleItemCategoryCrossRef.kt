package com.easyflow.diarycourse.data.models

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "schedule_item_category",
    primaryKeys = ["scheduleItemId", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = ScheduleItemDto::class,
            parentColumns = ["id"],
            childColumns = ["scheduleItemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryItemDto::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ScheduleItemCategoryCrossRef(
    val scheduleItemId: Int,
    val categoryId: Int
)