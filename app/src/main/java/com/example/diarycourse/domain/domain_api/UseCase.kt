package com.example.diarycourse.domain.domain_api

import com.example.diarycourse.data.models.ScheduleItem

interface UseCase {
    suspend fun insert(historyItem: ScheduleItem)

    suspend fun getAll(): List<ScheduleItem>

    suspend fun deleteById(itemId: Int)

    suspend fun deleteAll()
}