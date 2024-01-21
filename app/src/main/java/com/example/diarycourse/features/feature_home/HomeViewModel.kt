package com.example.diarycourse.features.feature_home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.diarycourse.data.repository_api.SharedRepository
import com.example.diarycourse.domain.domain_api.SharedUseCase
import com.example.diarycourse.domain.domain_api.UseCase
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.domain.util.Resource
import com.example.diarycourse.features.feature_schedule.ScheduleViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor (
    private val useCase: UseCase,
    private val sharedRepository: SharedRepository
) : ViewModel() {
    private val TAG = "debugTag"
    private val _dataList = MutableSharedFlow<List<ScheduleItem>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val dataList: SharedFlow<List<ScheduleItem>> = _dataList.asSharedFlow()

    fun fetchData() {
        viewModelScope.launch {
            _dataList.emitAll(useCase.getAll())
        }
    }

    fun updateSelectedDate(date: String) {
        viewModelScope.launch {
            Log.d(TAG, "выполнился updateSelectedDate")
            sharedRepository.updateSelectedDate(date)
        }
    }

//    fun fetchAdapterList() {
//        val selectedData: StateFlow<String> = sharedUseCase.selectedDateFlow
//    }

    class HomeViewModelFactory @Inject constructor(
        private val useCase: UseCase,
        private val sharedRepository: SharedRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(useCase, sharedRepository) as T
        }
    }
}