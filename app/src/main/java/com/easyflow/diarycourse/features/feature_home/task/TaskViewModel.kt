package com.easyflow.diarycourse.features.feature_home.task

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.domain_api.NoteUseCase
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_home.note.NoteViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleUseCase
) : BaseViewModel<TaskViewModel.State, TaskViewModel.Actions>(TaskViewModel.State()) {

    fun updateData(data: ScheduleItem) {
        viewModelScope.launch {
            val updateData = scheduleUseCase.update(data)
            modifyState { copy(update = updateData) }
        }
    }

    data class State(
        var update: Resource? = null
    )

    sealed interface Actions {
        data class ShowAlert(val alertData: String) : Actions
    }

    class TaskViewModelFactory @Inject constructor(
        private val scheduleUseCase: ScheduleUseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskViewModel(scheduleUseCase) as T
        }
    }
}