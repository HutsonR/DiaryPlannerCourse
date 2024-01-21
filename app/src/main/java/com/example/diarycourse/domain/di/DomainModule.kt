package com.example.diarycourse.domain.di

import com.example.diarycourse.data.database.ScheduleItemDao
import com.example.diarycourse.data.repository_api.Repository
import com.example.diarycourse.data.repository_api.SharedRepository
import com.example.diarycourse.data.repository_impl.RepositoryImpl
import com.example.diarycourse.data.repository_impl.SharedRepositoryImpl
import dagger.Module
import dagger.Provides

@Module
class DomainModule {
    @Provides
    fun provideRepository(scheduleItemDao: ScheduleItemDao): Repository {
        return RepositoryImpl(scheduleItemDao)
    }
    @Provides
    fun provideSharedRepository(): SharedRepository {
        return SharedRepositoryImpl()
    }
}
