package com.example.diarycourse.features.feature_schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

class ScheduleViewModel @Inject constructor (
    private val useCase: UseCase
) : ViewModel() {

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

    private val _update = MutableSharedFlow<Resource>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val update: SharedFlow<Resource> = _update.asSharedFlow()

    fun fetchData() {
        viewModelScope.launch {
            _dataList.emitAll(useCase.getAll())
        }
    }

    fun addData(data: ScheduleItem) {
        viewModelScope.launch {
            _result.emit(useCase.insert(data))
        }
    }

    fun updateData(data: ScheduleItem) {
        viewModelScope.launch {
            _update.emit(useCase.update(data))
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            _result.emit(useCase.deleteById(itemId))
        }
    }

    class NoteViewModelFactory @Inject constructor(
        private val useCase: UseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScheduleViewModel(useCase) as T
        }
    }

}