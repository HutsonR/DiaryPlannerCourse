package com.example.diarycourse.features.feature_home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diarycourse.domain.domain_api.ScheduleUseCase
import com.example.diarycourse.domain.models.ScheduleItem
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor (
    private val scheduleUseCase: ScheduleUseCase
) : ViewModel() {
    private val TAG = "debugTag"
    private val _dataList = MutableSharedFlow<List<ScheduleItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val dataList: SharedFlow<List<ScheduleItem>> = _dataList.asSharedFlow()

    fun fetchData() {
        viewModelScope.launch {
            _dataList.emitAll(scheduleUseCase.getAll())
        }
    }

//    fun fetchAdapterList() {
//        val selectedData: StateFlow<String> = sharedUseCase.selectedDateFlow
//    }

    class HomeViewModelFactory @Inject constructor(
        private val scheduleUseCase: ScheduleUseCase
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(scheduleUseCase) as T
        }
    }
}