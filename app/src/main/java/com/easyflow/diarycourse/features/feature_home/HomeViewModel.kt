package com.easyflow.diarycourse.features.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easyflow.diarycourse.domain.domain_api.NoteUseCase
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.domain.models.ScheduleItem
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor (
    private val scheduleUseCase: ScheduleUseCase,
    private val noteUseCase: NoteUseCase
) : ViewModel() {
    private val TAG = "debugTag"
    private val _dataList = MutableSharedFlow<List<ScheduleItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val dataList: SharedFlow<List<ScheduleItem>> = _dataList.asSharedFlow()

    private val _noteList = MutableSharedFlow<List<NoteItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val noteList: SharedFlow<List<NoteItem>> = _noteList.asSharedFlow()

    fun fetchData() {
        viewModelScope.launch {
            _dataList.emitAll(scheduleUseCase.getAll())
        }
        viewModelScope.launch {
            _noteList.emitAll(noteUseCase.getAll())
        }
    }

    class HomeViewModelFactory @Inject constructor(
        private val scheduleUseCase: ScheduleUseCase,
        private val noteUseCase: NoteUseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(scheduleUseCase, noteUseCase) as T
        }
    }
}