package com.easyflow.diarycourse

import android.app.Application
import com.easyflow.diarycourse.data.di.DataModule
import com.easyflow.diarycourse.domain.di.DomainModule
import com.easyflow.diarycourse.features.feature_home.HomeFragment
import com.easyflow.diarycourse.features.common.di.FeaturesModule
import com.easyflow.diarycourse.features.feature_home.note.NoteFragment
import com.easyflow.diarycourse.features.feature_home.schedule.ScheduleFragment
import com.easyflow.diarycourse.features.feature_home.schedule.dialogs.ScheduleItemBottomSheetFragment
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
