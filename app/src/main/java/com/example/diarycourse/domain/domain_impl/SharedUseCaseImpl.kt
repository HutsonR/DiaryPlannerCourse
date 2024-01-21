package com.example.diarycourse.domain.domain_impl

import androidx.lifecycle.LiveData
import com.example.diarycourse.data.repository_api.SharedRepository
import com.example.diarycourse.domain.domain_api.SharedUseCase
import com.example.diarycourse.domain.models.ScheduleItem
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

// SharedUseCaseImpl.kt

class SharedUseCaseImpl @Inject constructor(
    private val repository: SharedRepository
) : SharedUseCase {
    override fun updateSelectedDate(date: String) {
        return repository.updateSelectedDate(date)
    }
    override fun observeSelectedDate(): LiveData<String> {
        return repository.observeSelectedDate()
    }
}
