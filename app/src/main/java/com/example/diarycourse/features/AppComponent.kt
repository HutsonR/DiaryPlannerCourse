package com.example.diarycourse.features

import android.app.Application
import com.example.diarycourse.data.di.DataModule
import com.example.diarycourse.domain.di.DomainModule
import com.example.diarycourse.features.di.FeaturesModule
import com.example.diarycourse.features.dialogs.ScheduleItemBottomSheetFragment
import com.example.diarycourse.features.ui.NoteFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Component(modules = [DataModule::class, DomainModule::class, FeaturesModule::class])
interface AppComponent {

    fun inject(fragment: NoteFragment)
    fun inject(fragment: ScheduleItemBottomSheetFragment)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(application: Application): Builder

        fun build(): AppComponent
    }
}
