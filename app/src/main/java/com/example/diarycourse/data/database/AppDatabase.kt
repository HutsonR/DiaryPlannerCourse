package com.example.diarycourse.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.diarycourse.data.models.ScheduleItem

@Database(entities = [ScheduleItem::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract val scheduleItemDao: ScheduleItemDao

    companion object {
        const val DATABASE_NAME = "schedule_database"

//        @Volatile
//        private var instance: AppDatabase? = null
//
//        fun getInstance(context: Context): AppDatabase {
//            return instance ?: synchronized(this) {
//                instance ?: buildDatabase(context).also { instance = it }
//            }
//        }
//
//        private fun buildDatabase(context: Context): AppDatabase {
//            return Room.databaseBuilder(
//                context.applicationContext,
//                AppDatabase::class.java,
//                DATABASE_NAME
//            ).fallbackToDestructiveMigration().build()
//        }
    }
}
