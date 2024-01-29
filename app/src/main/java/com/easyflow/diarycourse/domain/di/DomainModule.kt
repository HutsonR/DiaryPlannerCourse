package com.easyflow.diarycourse.domain.di

import com.easyflow.diarycourse.data.database.NoteItemDao
import com.easyflow.diarycourse.data.database.ScheduleItemDao
import com.easyflow.diarycourse.data.repository_api.NoteRepository
import com.easyflow.diarycourse.data.repository_api.ScheduleRepository
import com.easyflow.diarycourse.data.repository_impl.NoteRepositoryImpl
import com.easyflow.diarycourse.data.repository_impl.ScheduleRepositoryImpl
import dagger.Module
import dagger.Provides

@Module
class DomainModule {
    @Provides
    fun provideScheduleRepository(scheduleItemDao: ScheduleItemDao): ScheduleRepository {
        return ScheduleRepositoryImpl(scheduleItemDao)
    }
    @Provides
    fun provideNoteRepository(noteItemDao: NoteItemDao): NoteRepository {
        return NoteRepositoryImpl(noteItemDao)
    }
}
