package com.example.diarycourse.domain.di

import com.example.diarycourse.data.database.NoteItemDao
import com.example.diarycourse.data.database.ScheduleItemDao
import com.example.diarycourse.data.repository_api.NoteRepository
import com.example.diarycourse.data.repository_api.ScheduleRepository
import com.example.diarycourse.data.repository_impl.NoteRepositoryImpl
import com.example.diarycourse.data.repository_impl.ScheduleRepositoryImpl
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
