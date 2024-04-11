package com.easyflow.diarycourse.core

import android.app.Application
import com.easyflow.diarycourse.data.di.DataModule
import com.easyflow.diarycourse.domain.di.DomainModule
import com.easyflow.diarycourse.features.common.di.FeaturesModule
import com.easyflow.diarycourse.features.feature_calendar.CalendarFragment
import com.easyflow.diarycourse.features.feature_calendar.note.NoteFragment
import com.easyflow.diarycourse.features.feature_calendar.schedule.dialogs.ScheduleItemBottomSheetFragment
import com.easyflow.diarycourse.features.feature_calendar.task.TaskFragment
import com.easyflow.diarycourse.features.feature_calendar.task.dialogs.ReminderDialogFragment
import com.easyflow.diarycourse.features.feature_settings.SettingsFragment
import com.easyflow.diarycourse.features.feature_settings.appearance.AppearanceFragment
import dagger.BindsInstance
import dagger.Component

@Component(modules = [DataModule::class, DomainModule::class, FeaturesModule::class])
interface AppComponent {

    fun inject(fragment: CalendarFragment)
    fun inject(fragment: NoteFragment)
    fun inject(fragment: ScheduleItemBottomSheetFragment)
    fun inject(fragment: TaskFragment)
    fun inject(fragment: ReminderDialogFragment)
    fun inject(fragment: SettingsFragment)
    fun inject(fragment: AppearanceFragment)

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(application: Application): Builder

        fun build(): AppComponent
    }
}
