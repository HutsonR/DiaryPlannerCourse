package com.example.diarycourse.features.common

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diarycourse.domain.domain_api.UseCase
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.features.feature_schedule.ScheduleViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class SharedViewModel @Inject constructor () : ViewModel() {

    private val _selectedDate = MutableSharedFlow<String>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val selectedDate: SharedFlow<String> = _selectedDate.asSharedFlow()

    private val _adapterList = MutableSharedFlow<List<ScheduleItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val adapterList: SharedFlow<List<ScheduleItem>> = _adapterList.asSharedFlow()

    fun updateSelectedDate(date: String) {
        viewModelScope.launch {
            _selectedDate.emit(date)
        }
    }

    fun updateScheduleDataList(dataList: List<ScheduleItem>) {
        viewModelScope.launch {
            _adapterList.emit(dataList)
        }
    }

    class SharedViewModelFactory @Inject constructor() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SharedViewModel() as T
        }
    }
}