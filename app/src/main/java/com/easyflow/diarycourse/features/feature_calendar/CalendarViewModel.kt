package com.easyflow.diarycourse.features.feature_calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.core.BaseViewModel
import com.easyflow.diarycourse.domain.domain_api.NoteUseCase
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.models.NoteItem
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
    private val scheduleUseCase: ScheduleUseCase,
    private val noteUseCase: NoteUseCase
) : BaseViewModel<CalendarViewModel.State, CalendarViewModel.Actions>(CalendarViewModel.State()) {

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

    fun fetchDataByDate(date: String) {
        viewModelScope.launch {
            val taskItems = scheduleUseCase.getByDate(date)

            modifyState {
                copy(
                    selectedDate = date,
                    selectedTasks = sortItemsByTime(taskItems)
                )
            }
        }
    }

    private fun sortItemsByTime(dataList: List<ScheduleItem>): List<ScheduleItem> {
        return dataList.sortedBy { it.startTime }
    }

    private fun fetchData() {
        viewModelScope.launch {
            val fetchList: MutableList<ScheduleItem> = mutableListOf()

            coroutineScope {
                val scheduleItemsDeferred = async { scheduleUseCase.getAll() }

                val scheduleItemsAwait = scheduleItemsDeferred.await()
                fetchList.addAll(scheduleItemsAwait)
            }

            if (fetchList.isNotEmpty()) {
                modifyState {
                    copy(list = fetchList)
                }
            }
        }
    }

    // Если note нет, то задаём дату для него, чтобы при передаче мы создали заметку на этот день
    private fun createNote(note: NoteItem?): NoteItem {
        return note
            ?: NoteItem(
                text = "",
                date = getState().selectedDate
            )
    }

    private fun sortTaskByGroup() {

    }

//    Actions
    fun goToTask() {
        onAction(Actions.GoToTask)
    }

    fun goToNote() {
        Log.d("debugTag", "goToNote note ${getState().note}")
        val noteItem = noteUseCase.getNote(getState().selectedDate)
        onAction(Actions.GoToNote(createNote(noteItem)))
    }

    data class State(
        var selectedDate: String = "",
        var list: List<ScheduleItem> = emptyList(),
        var selectedTasks: List<ScheduleItem> = emptyList(),
        var note: NoteItem? = null
    )

    sealed interface Actions {
        data class ShowAlert(val alertData: String) : Actions
        data class GoToNote(val note: NoteItem) : Actions
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