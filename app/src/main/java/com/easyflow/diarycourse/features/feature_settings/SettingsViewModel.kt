package com.easyflow.diarycourse.features.feature_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import javax.inject.Inject

class SettingsViewModel @Inject constructor() : BaseViewModel<SettingsViewModel.State, SettingsViewModel.Actions>(SettingsViewModel.State()) {

    fun goToAppearance() {
        onAction(Actions.GoToAppearance)
    }

    fun goToSecurity() {
        onAction(Actions.GoToSecurity)
    }

    data class State(
        var list: String = ""
    )

    sealed interface Actions {
        data object GoToAppearance : Actions
        data object GoToSecurity : Actions
    }

    class SettingsViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel() as T
        }
    }
}