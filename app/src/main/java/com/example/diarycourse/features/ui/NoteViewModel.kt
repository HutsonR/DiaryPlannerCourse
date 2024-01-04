package com.example.diarycourse.features.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diarycourse.data.database.ScheduleItemDao
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.domain.domain_api.UseCase
import com.example.diarycourse.domain.util.Resource
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class NoteViewModel @Inject constructor (
    private val useCase: UseCase,
    private val scheduleItemDao: ScheduleItemDao,
    application: Application
) : ViewModel() {

    private val TAG = "debugTag"
//    private val appContext: Context = application.applicationContext
//    private var dataList: MutableList<ScheduleItem> = mutableListOf()

    private val _dataList = MutableSharedFlow<List<ScheduleItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val dataList: SharedFlow<List<ScheduleItem>> = _dataList.asSharedFlow()

    private val _result = MutableSharedFlow<Resource>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val result: SharedFlow<Resource> = _result.asSharedFlow()

    fun getData() {
        viewModelScope.launch {
            _dataList.emitAll(useCase.getAll())
        }
    }

    fun addData(data: ScheduleItem) {
        viewModelScope.launch {
            _result.emit(useCase.insert(data))
        }
    }

    class NoteViewModelFactory @Inject constructor(
        private val useCase: UseCase,
        private val scheduleItemDao: ScheduleItemDao,
        private val application: Application
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NoteViewModel(useCase, scheduleItemDao, application) as T
        }
    }

}