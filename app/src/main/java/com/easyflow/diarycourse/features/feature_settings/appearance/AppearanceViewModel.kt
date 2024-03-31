package com.easyflow.diarycourse.features.feature_settings.appearance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.features.feature_calendar.models.CombineModel
import javax.inject.Inject

class AppearanceViewModel @Inject constructor() : BaseViewModel<AppearanceViewModel.State, AppearanceViewModel.Actions>(AppearanceViewModel.State()) {

    fun goBack() {
        onAction(Actions.GoBack)
    }

    fun themeSwitchMode(mode: Int) {
        onAction(Actions.SwitchTheme(mode))
    }

    fun switchNavigation(mode: Int) {
        onAction(Actions.SwitchNavigation(mode))
    }

    data class State(
        var list: List<CombineModel> = emptyList()
    )

    sealed interface Actions {
        data object GoBack : Actions
        data class SwitchTheme(val mode: Int) : Actions
        data class SwitchNavigation(val mode: Int) : Actions
    }

    class AppearanceViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppearanceViewModel() as T
        }
    }
}