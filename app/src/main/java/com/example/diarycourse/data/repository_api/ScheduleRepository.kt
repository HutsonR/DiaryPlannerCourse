package com.example.diarycourse.data.repository_api

import com.example.diarycourse.domain.models.ScheduleItem
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    suspend fun insert(item: ScheduleItem)

    suspend fun getAll(): Flow<List<ScheduleItem>>

    suspend fun deleteById(itemId: Int)

    suspend fun deleteAll()

    suspend fun update(item: ScheduleItem)
}