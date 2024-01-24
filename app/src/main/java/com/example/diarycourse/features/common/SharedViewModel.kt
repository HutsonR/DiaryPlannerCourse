package com.example.diarycourse.features.common

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diarycourse.domain.models.ScheduleItem
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedViewModel @Inject constructor () : ViewModel() {

    val selectedDate = MutableLiveData<String>()

    private val _adapterList = MutableSharedFlow<List<ScheduleItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val adapterList: SharedFlow<List<ScheduleItem>> = _adapterList.asSharedFlow()

    fun updateSelectedDate(date: String) {
        Log.d("debugTag", "выполнился updateSelectedDate")
        viewModelScope.launch {
            selectedDate.postValue(date)
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