package com.easyflow.diarycourse.features.feature_home.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.models.ScheduleItem
import javax.inject.Inject

class InboxViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleUseCase
) : BaseViewModel<InboxViewModel.State, InboxViewModel.Actions>(State()) {

    fun goBack() {
        onAction(Actions.GoBack)
    }

    data class State(
        var list: List<ScheduleItem> = emptyList()
    )

    sealed interface Actions {
        data object GoBack : Actions
        data class ShowAlert(val alertData: String) : Actions
    }

    class InboxViewModelFactory @Inject constructor(
        private val scheduleUseCase: ScheduleUseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InboxViewModel(scheduleUseCase) as T
        }
    }
}