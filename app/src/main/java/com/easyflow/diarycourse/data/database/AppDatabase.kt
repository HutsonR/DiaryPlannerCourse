package com.easyflow.diarycourse.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.easyflow.diarycourse.data.models.NoteItemDto
import com.easyflow.diarycourse.data.models.ScheduleItemDto

@Database(entities = [ScheduleItemDto::class, NoteItemDto::class], version = 10)
abstract class AppDatabase : RoomDatabase() {
    abstract val scheduleItemDao: ScheduleItemDao
    abstract val noteItemDao: NoteItemDao

    companion object {
        const val DATABASE_NAME = "schedule_database"
    }
}
