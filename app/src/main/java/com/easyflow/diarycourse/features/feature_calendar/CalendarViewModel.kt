package com.easyflow.diarycourse.features.feature_calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.core.utils.formatDate
import com.easyflow.diarycourse.domain.domain_api.NoteUseCase
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource
import com.easyflow.diarycourse.features.feature_calendar.models.CombineModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

class CalendarViewModel @Inject constructor(
    private val scheduleUseCase: ScheduleUseCase,
    private val noteUseCase: NoteUseCase
) : BaseViewModel<CalendarViewModel.State, CalendarViewModel.Actions>(CalendarViewModel.State()) {

    private val _result = MutableSharedFlow<Resource>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val result: SharedFlow<Resource> = _result.asSharedFlow()

    private var dateSelected = ""

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

    fun onChangeDate(date: String) {
        Log.d("debugTag", "VM onChangeDate $date")
        modifyState { copy(dateSelected = date) }
        dateSelected = date
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

    fun fetchTasksByDate(date: String) {
        viewModelScope.launch {
            val taskItems = scheduleUseCase.getByDate(date)
            modifyState { copy(selectedTasks = taskItems) }
        }
    }

    fun fetchData() {
        Log.d("debugTag", "VM fetchData")
        viewModelScope.launch {
            val combineModels: MutableList<CombineModel> = mutableListOf()

            coroutineScope {
                val scheduleItemsDeferred = async { scheduleUseCase.getAll() }
                val noteItemsDeferred = async { noteUseCase.getAll() }

                // Ждем выполнения обеих корутин
                val scheduleItems = scheduleItemsDeferred.await()
                val noteItems = noteItemsDeferred.await()

                // Создаем список CombineModel и добавляем в него элементы из обеих списков
                combineModels.addAll(
                    scheduleItems.map { scheduleItem ->
                        CombineModel(
                            id = scheduleItem.id,
                            text = scheduleItem.text,
                            description = scheduleItem.description,
                            date = scheduleItem.date,
                            startTime = scheduleItem.startTime,
                            endTime = scheduleItem.endTime,
                            duration = scheduleItem.duration,
                            color = scheduleItem.color,
                            isCompleteTask = scheduleItem.isCompleteTask,
                            priority = scheduleItem.priority
                        )
                    }
                )
                combineModels.addAll(
                    noteItems.map { noteItem ->
                        CombineModel(
                            id = noteItem.id,
                            text = noteItem.text,
                            date = noteItem.date
                        )
                    }
                )
            }

            if (combineModels.isNotEmpty()) {
                modifyState { copy(list = combineModels) }
            }
        }
    }

//    Actions
    fun goToTask() {
        onAction(Actions.GoToTask)
    }

    data class State(
        var list: List<CombineModel> = emptyList(),
        var selectedTasks: List<ScheduleItem> = emptyList(),
        var sortedTasks: List<ScheduleItem> = emptyList(),
        var dateSelected: String = ""
    )

    sealed interface Actions {
        data class ShowAlert(val alertData: String) : Actions
        data object GoToTask : Actions
    }

    class CalendarViewModelFactory @Inject constructor(
        private val scheduleUseCase: ScheduleUseCase,
        private val noteUseCase: NoteUseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(scheduleUseCase, noteUseCase) as T
        }
    }
}