package com.example.diarycourse.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.diarycourse.data.models.ScheduleItemDto
import com.example.diarycourse.domain.models.ScheduleItem

@Database(entities = [ScheduleItemDto::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract val scheduleItemDao: ScheduleItemDao

    companion object {
        const val DATABASE_NAME = "schedule_database"
    }
}
