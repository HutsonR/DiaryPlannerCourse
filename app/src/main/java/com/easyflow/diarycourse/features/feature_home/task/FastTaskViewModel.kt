package com.easyflow.diarycourse.features.feature_home.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.models.ScheduleItem
import javax.inject.Inject

class FastTaskViewModel @Inject constructor() :
    BaseViewModel<FastTaskViewModel.State, FastTaskViewModel.Actions>(State()) {

    fun goBack() {
        onAction(Actions.GoBack)
    }

    fun openDurationDialog() {
        onAction(Actions.GoToDurationDialog)
    }

    fun openPriorityDialog() {
        onAction(Actions.GoToPriorityDialog)
    }

    fun openColorDialog() {
        onAction(Actions.GoToColorDialog)
    }

    fun updateTask(item: ScheduleItem) {
        modifyState { copy(task = item) }
        updateSaveButtonState()
    }

    fun updateSaveButtonState() {
        val isEnable = getState().task?.text?.isNotEmpty()
        onAction(Actions.UpdateSaveButtonState(isEnable ?: false))
    }

    data class State(
        var task: ScheduleItem? = null
    )

    sealed interface Actions {
        data object GoBack : Actions
        data object GoToDurationDialog : Actions
        data object GoToPriorityDialog : Actions
        data object GoToColorDialog : Actions
        data class UpdateSaveButtonState(val state: Boolean) : Actions
    }

    class FastTaskViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FastTaskViewModel() as T
        }
    }
}