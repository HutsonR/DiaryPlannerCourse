package com.example.diarycourse.data.repository_impl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.diarycourse.annotations.AppScope
import com.example.diarycourse.data.repository_api.SharedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
@AppScope
class SharedRepositoryImpl @Inject constructor(): SharedRepository {

//    private val _selectedDateFlow = MutableStateFlow<String>()
//    val selectedDateFlow: StateFlow<String> = _selectedDateFlow.asStateFlow()
//
//    override fun updateSelectedDate(date: String) {
//        _selectedDateFlow.value = date
//    }
//
//    override fun observeSelectedDate(): StateFlow<String> {
//        return selectedDateFlow
//    }

    private val selectedDateLiveData = MutableLiveData<String>()
    private val aga: String = ""
    override fun updateSelectedDate(date: String) {
        selectedDateLiveData.value = date
    }

    override fun observeSelectedDate(): LiveData<String> {
        return selectedDateLiveData
    }


//    private val selectedDateLiveData = MutableLiveData<String>()
//    private val dataListLiveData = MutableLiveData<List<ScheduleItem>>()

//
//    override fun setSelectedDate(selectedDate: String) {
//        selectedDateLiveData.value = selectedDate
//    }
//
//    override fun observeSelectedDate(): LiveData<String> {
//        return selectedDateLiveData
//    }
//
//    override fun setDataList(dataList: List<ScheduleItem>) {
//        dataListLiveData.value = dataList
//    }
//
//    override fun observeDataList(): LiveData<List<ScheduleItem>> {
//        return dataListLiveData
//    }
}
