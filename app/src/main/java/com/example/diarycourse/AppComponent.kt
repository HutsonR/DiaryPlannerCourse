package com.example.diarycourse

import android.app.Application
import com.example.diarycourse.data.di.DataModule
import com.example.diarycourse.domain.di.DomainModule
import com.example.diarycourse.features.feature_home.HomeFragment
import com.example.diarycourse.features.common.di.FeaturesModule
import com.example.diarycourse.features.feature_home.note.NoteFragment
import com.example.diarycourse.features.feature_home.schedule.ScheduleFragment
import com.example.diarycourse.features.feature_home.schedule.dialogs.ScheduleItemBottomSheetFragment
import dagger.BindsInstance
import dagger.Component

@Component(modules = [DataModule::class, DomainModule::class, FeaturesModule::class])
interface AppComponent {

    fun inject(fragment: HomeFragment)
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
