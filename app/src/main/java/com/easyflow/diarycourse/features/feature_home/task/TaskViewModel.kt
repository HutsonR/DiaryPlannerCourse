package com.easyflow.diarycourse.features.feature_home.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.util.Resource
import javax.inject.Inject

class TaskViewModel @Inject constructor() : BaseViewModel<TaskViewModel.State, TaskViewModel.Actions>(TaskViewModel.State()) {

    data class State(
        var update: Resource? = null
    )

    sealed interface Actions {
        data class ShowAlert(val alertData: String) : Actions
    }

    class TaskViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskViewModel() as T
        }
    }
}