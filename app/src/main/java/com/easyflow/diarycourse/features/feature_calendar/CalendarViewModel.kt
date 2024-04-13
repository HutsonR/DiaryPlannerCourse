package com.easyflow.diarycourse.features.feature_calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class CalendarViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleUseCase
) : BaseViewModel<CalendarViewModel.State, CalendarViewModel.Actions>(CalendarViewModel.State()) {

    private var list: MutableList<ScheduleItem> = mutableListOf()

    private val _result = MutableSharedFlow<Resource>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val result: SharedFlow<Resource> = _result.asSharedFlow()

    init {
        fetchData()
    }

    fun addData(data: ScheduleItem) {
        viewModelScope.launch {
            val addData = scheduleUseCase.insert(data)
            _result.emit(addData)
        }
    }

    fun updateData(data: ScheduleItem) {
        viewModelScope.launch {
            val updateData = scheduleUseCase.update(data)
            _result.emit(updateData)
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            val deleteItem = scheduleUseCase.deleteById(itemId)
            _result.emit(deleteItem)
        }
    }

    fun fetchTasksByDate(date: String) {
        viewModelScope.launch {
            val taskItems = scheduleUseCase.getByDate(date)
            modifyState { copy(selectedTasks = taskItems) }
        }
    }

    private fun fetchData() {
        viewModelScope.launch {
            val fetchList: MutableList<ScheduleItem> = mutableListOf()

            coroutineScope {
                val scheduleItemsDeferred = async { scheduleUseCase.getAll() }
                val scheduleItems = scheduleItemsDeferred.await()
                fetchList.addAll(scheduleItems)
                list.addAll(scheduleItems)
            }

            if (fetchList.isNotEmpty()) {
                modifyState { copy(list = fetchList) }
            }
        }
    }

    private fun sortTaskByGroup() {

    }

//    Actions
    fun goToTask() {
        onAction(Actions.GoToTask)
    }

    data class State(
        var list: List<ScheduleItem> = emptyList(),
        var selectedTasks: List<ScheduleItem> = emptyList(),
        var dateSelected: String = ""
    )

    sealed interface Actions {
        data class ShowAlert(val alertData: String) : Actions
        data object GoToTask : Actions
    }

    class CalendarViewModelFactory @Inject constructor(
        private val scheduleUseCase: ScheduleUseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(scheduleUseCase) as T
        }
    }
}