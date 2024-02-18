package com.easyflow.diarycourse.features.feature_home.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_home.HomeViewModel
import com.easyflow.diarycourse.features.feature_home.models.CombineModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class ScheduleViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleUseCase
) : BaseViewModel<ScheduleViewModel.State, ScheduleViewModel.Actions>(ScheduleViewModel.State()) {

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            val fetchData = scheduleUseCase.getAll()
            modifyState { copy(list = fetchData) }
        }
    }

    fun addData(data: ScheduleItem) {
        viewModelScope.launch {
            val addData = scheduleUseCase.insert(data)
            modifyState { copy(result = addData) }
        }
    }

    fun updateData(data: ScheduleItem) {
        viewModelScope.launch {
            val updateData = scheduleUseCase.update(data)
            modifyState { copy(update = updateData) }
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            val deleteItem = scheduleUseCase.deleteById(itemId)
            modifyState { copy(result = deleteItem) }
        }
    }

    data class State(
        var list: List<ScheduleItem> = emptyList(),
        var result: Resource? = null,
        var update: Resource? = null
    )

    sealed interface Actions {
        data class ShowAlert(val alertData: String) : Actions
    }

    class ScheduleViewModelFactory @Inject constructor(
        private val scheduleUseCase: ScheduleUseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScheduleViewModel(scheduleUseCase) as T
        }
    }

}