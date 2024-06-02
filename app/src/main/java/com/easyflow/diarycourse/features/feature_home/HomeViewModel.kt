package com.easyflow.diarycourse.features.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.models.ScheduleItem
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleUseCase
) : BaseViewModel<HomeViewModel.State, HomeViewModel.Actions>(State()) {
    // TODO сделать UseCase для Inbox

    fun fastAddTask() {
        onAction(Actions.GoToFastAddTask)
    }

    fun goToInbox() {
        onAction(Actions.GoToInbox)
    }

    fun getInboxItemsCount() =
        getState().inboxList.size.toString()

    data class State(
        var inboxList: List<Any> = emptyList(),
        var list: List<ScheduleItem> = emptyList()
    )

    sealed interface Actions {
        data object GoToInbox : Actions
        data object GoToFastAddTask : Actions
        data class ShowAlert(val alertData: String) : Actions
    }

    class HomeViewModelFactory @Inject constructor(
        private val scheduleUseCase: ScheduleUseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(scheduleUseCase) as T
        }
    }
}