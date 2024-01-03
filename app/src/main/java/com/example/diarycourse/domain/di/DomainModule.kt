package com.example.diarycourse.domain.di

import com.example.diarycourse.data.database.ScheduleItemDao
import com.example.diarycourse.data.repository_api.Repository
import com.example.diarycourse.data.repository_impl.RepositoryImpl
import dagger.Module
import dagger.Provides

@Module
class DomainModule {
    @Provides
    fun provideRepository(scheduleItemDao: ScheduleItemDao): Repository {
        return RepositoryImpl(scheduleItemDao)
    }
}
