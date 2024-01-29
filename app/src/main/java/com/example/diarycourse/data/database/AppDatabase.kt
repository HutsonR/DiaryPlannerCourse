package com.example.diarycourse.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.diarycourse.data.models.NoteItemDto
import com.example.diarycourse.data.models.ScheduleItemDto
import com.example.diarycourse.domain.models.ScheduleItem

@Database(entities = [ScheduleItemDto::class, NoteItemDto::class], version = 8)
abstract class AppDatabase : RoomDatabase() {
    abstract val scheduleItemDao: ScheduleItemDao
    abstract val noteItemDao: NoteItemDao

    companion object {
        const val DATABASE_NAME = "schedule_database"
    }
}
