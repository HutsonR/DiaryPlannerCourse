package com.example.diarycourse.data.di

import android.app.Application
import androidx.room.Room
import com.example.diarycourse.data.database.AppDatabase
import com.example.diarycourse.data.database.ScheduleItemDao
import dagger.Module
import dagger.Provides

@Module
class DataModule {

    @Provides
    fun provideAppDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    fun provideScheduleItemDao(appDatabase: AppDatabase): ScheduleItemDao {
        return appDatabase.scheduleItemDao
    }
}
