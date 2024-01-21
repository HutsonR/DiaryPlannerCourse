package com.example.diarycourse.domain.domain_api

import androidx.lifecycle.LiveData
import com.example.diarycourse.domain.models.ScheduleItem
import kotlinx.coroutines.flow.StateFlow

interface SharedUseCase {
    fun updateSelectedDate(date: String)
    fun observeSelectedDate(): LiveData<String>
}