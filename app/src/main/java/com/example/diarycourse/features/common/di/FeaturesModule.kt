package com.example.diarycourse.features.common.di

import com.example.diarycourse.data.repository_api.NoteRepository
import com.example.diarycourse.data.repository_api.ScheduleRepository
import com.example.diarycourse.domain.domain_api.NoteUseCase
import com.example.diarycourse.domain.domain_api.ScheduleUseCase
import com.example.diarycourse.domain.domain_impl.NoteUseCaseImpl
import com.example.diarycourse.domain.domain_impl.ScheduleUseCaseImpl
import dagger.Module
import dagger.Provides

@Module
class FeaturesModule {
    @Provides
    fun provideScheduleUseCase(scheduleRepository: ScheduleRepository): ScheduleUseCase {
        return ScheduleUseCaseImpl(scheduleRepository)
    }
    @Provides
    fun provideNoteUseCase(noteRepository: NoteRepository): NoteUseCase {
        return NoteUseCaseImpl(noteRepository)
    }
}