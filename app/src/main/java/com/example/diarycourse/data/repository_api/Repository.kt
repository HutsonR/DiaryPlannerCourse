package com.example.diarycourse.data.repository_api

import com.example.diarycourse.data.models.ScheduleItem

interface Repository {
    suspend fun insert(historyItem: ScheduleItem)

    suspend fun getAll(): List<ScheduleItem>

    suspend fun deleteById(itemId: Int)

    suspend fun deleteAll()
}