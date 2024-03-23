package com.easyflow.diarycourse.features.feature_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.features.feature_calendar.models.CombineModel
import javax.inject.Inject

class SettingsViewModel @Inject constructor() : BaseViewModel<SettingsViewModel.State, SettingsViewModel.Actions>(SettingsViewModel.State()) {

    data class State(
        var list: List<CombineModel> = emptyList()
    )

    sealed interface Actions {
        data class ShowAlert(val alertData: String) : Actions
    }

    class SettingsViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel() as T
        }
    }
}