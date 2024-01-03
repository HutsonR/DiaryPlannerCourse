package com.example.diarycourse.domain.di

import com.example.diarycourse.data.database.ScheduleItemDao
import com.example.diarycourse.data.repository_api.Repository
import com.example.diarycourse.data.repository_impl.RepositoryImpl
import com.example.diarycourse.domain.domain_api.UseCase
import com.example.diarycourse.domain.domain_impl.UseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DomainModule {
    @Provides
    fun provideRepository(scheduleItemDao: ScheduleItemDao): Repository {
        return RepositoryImpl(scheduleItemDao)
    }
}
