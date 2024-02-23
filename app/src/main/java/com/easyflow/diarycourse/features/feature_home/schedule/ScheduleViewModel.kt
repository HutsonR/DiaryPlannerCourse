package com.easyflow.diarycourse.features.feature_home.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.util.Resource
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ScheduleViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleUseCase
) : BaseViewModel<ScheduleViewModel.State, ScheduleViewModel.Actions>(ScheduleViewModel.State()) {

    private val _result = MutableSharedFlow<Resource>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val result: SharedFlow<Resource> = _result.asSharedFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            val fetchData = scheduleUseCase.getAll()
            Log.d("debugTag", "SCHEDULE fetchData $fetchData")
            modifyState { copy(list = fetchData) }
        }
    }

    fun addData(data: ScheduleItem) {
        viewModelScope.launch {
            val addData = scheduleUseCase.insert(data)
            Log.d("debugTag", "SCHEDULE addData $addData")
            _result.emit(addData)
        }
    }

    fun updateData(data: ScheduleItem) {
        viewModelScope.launch {
            val updateData = scheduleUseCase.update(data)
            Log.d("debugTag", "SCHEDULE updateData $updateData")
            _result.emit(updateData)
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            val deleteItem = scheduleUseCase.deleteById(itemId)
            Log.d("debugTag", "SCHEDULE deleteItem $deleteItem")
            _result.emit(deleteItem)
        }
    }

    data class State(
        var list: List<ScheduleItem> = emptyList()
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