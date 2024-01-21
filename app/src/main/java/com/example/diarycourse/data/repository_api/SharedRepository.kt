package com.example.diarycourse.data.repository_api

import androidx.lifecycle.LiveData
import com.example.diarycourse.domain.models.ScheduleItem
import kotlinx.coroutines.flow.StateFlow

interface SharedRepository {
    fun updateSelectedDate(date: String)
    fun observeSelectedDate(): LiveData<String>
//    fun setSelectedDate(selectedDate: String)
//    fun observeSelectedDate(): LiveData<String>
//    fun setDataList(dataList: List<ScheduleItem>)
//    fun observeDataList(): LiveData<List<ScheduleItem>>
}