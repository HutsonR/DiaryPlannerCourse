package com.easyflow.diarycourse.features.feature_settings.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import javax.inject.Inject

class SecurityViewModel @Inject constructor() : BaseViewModel<SecurityViewModel.State, SecurityViewModel.Actions>(SecurityViewModel.State()) {

    fun goBack() {
        onAction(Actions.GoBack)
    }

    fun makeFingerprint() {
        onAction(Actions.SwitchFingerprint)
    }

    data class State(
        var list: String = ""
    )

    sealed interface Actions {
        data object GoBack : Actions
        data object SwitchFingerprint : Actions
    }

    class SecurityViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SecurityViewModel() as T
        }
    }
}