package com.easyflow.diarycourse.features.feature_calendar.schedule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.core.utils.formatDate
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

class ScheduleViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleUseCase
) : BaseViewModel<ScheduleViewModel.State, ScheduleViewModel.Actions>(ScheduleViewModel.State()) {

    private val _result = MutableSharedFlow<Resource>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val result: SharedFlow<Resource> = _result.asSharedFlow()

    private var dateSelected = ""

    init {
        fetchData()
    }

    fun fetchData() {
        Log.d("debugTag", "VM dateSelected $dateSelected")
        viewModelScope.launch {
            val fetchData = scheduleUseCase.getAll()
            modifyState { copy(tasks = fetchData) }
        }
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

    fun onChangeDate(date: String) {
        Log.d("debugTag", "VM onChangeDate $date")
        modifyState { copy(dateSelected = date) }
        dateSelected = date
        sortItems(getState().tasks)
    }

    private fun sortItemsByDate(dataList: List<ScheduleItem>): List<ScheduleItem> {
        val sortedData: MutableList<ScheduleItem> = mutableListOf()
        return if (dateSelected.isNotEmpty()) {
            dataList.forEach {
                if (it.date == dateSelected)
                    sortedData.add(it)
            }
            sortedData
        } else {
            val today = Calendar.getInstance()
            dateSelected = formatDate(today)
            sortItemsByDate(dataList)
        }
    }

    private fun sortItemsByTime(dataList: List<ScheduleItem>): List<ScheduleItem> {
        return dataList.sortedBy { it.startTime }
    }

    fun sortItems(dataList: List<ScheduleItem>) {
        val sortedDataByDate = sortItemsByDate(dataList)
        val resultSortedItems = sortItemsByTime(sortedDataByDate)

        modifyState { copy(sortedTasks = resultSortedItems) }
    }

    data class State(
        var tasks: List<ScheduleItem> = emptyList(),
        var sortedTasks: List<ScheduleItem> = emptyList(),
        var dateSelected: String = ""
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