package com.example.diarycourse.features

import android.app.Application
import com.example.diarycourse.data.di.DataModule
import com.example.diarycourse.domain.di.DomainModule
import com.example.diarycourse.features.common.di.FeaturesModule
import com.example.diarycourse.features.feature_note.NoteFragment
import com.example.diarycourse.features.feature_schedule.dialogs.ScheduleItemBottomSheetFragment
import com.example.diarycourse.features.feature_schedule.ScheduleFragment
import dagger.BindsInstance
import dagger.Component

@Component(modules = [DataModule::class, DomainModule::class, FeaturesModule::class])
interface AppComponent {

    fun inject(fragment: ScheduleFragment)
    fun inject(fragment: NoteFragment)
    fun inject(fragment: ScheduleItemBottomSheetFragment)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(application: Application): Builder

        fun build(): AppComponent
    }
}
