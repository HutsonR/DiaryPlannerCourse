package com.easyflow.diarycourse.features.feature_calendar.task.dialogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import javax.inject.Inject

class ReminderDialogViewModel @Inject constructor() : BaseViewModel<ReminderDialogViewModel.State, ReminderDialogViewModel.Actions>(ReminderDialogViewModel.State()) {

    private var time: String = ""

    fun saveButtonCheck(): Boolean {
        return time.isNotEmpty()
    }

    fun setTime(time: String) {
        if (time.isNotEmpty()) {
            this.time = time
            modifyState { copy(time = time) }
        }
    }

    fun sendItem(isEmpty: Boolean = false) {
        var item = ""
        if (!isEmpty) {
            item = time
        }
        onAction(Actions.GoBack(item))
    }

    data class State(
        var time: String = ""
    )

    sealed interface Actions {
        data class GoBack(val item: String) : Actions
    }

    class ReminderViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReminderDialogViewModel() as T
        }
    }
}