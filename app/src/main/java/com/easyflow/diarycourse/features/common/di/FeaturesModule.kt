package com.easyflow.diarycourse.features.common.di

import com.easyflow.diarycourse.data.repository_api.NoteRepository
import com.easyflow.diarycourse.data.repository_api.ScheduleRepository
import com.easyflow.diarycourse.domain.domain_api.NoteUseCase
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.domain_impl.NoteUseCaseImpl
import com.easyflow.diarycourse.domain.domain_impl.ScheduleUseCaseImpl
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