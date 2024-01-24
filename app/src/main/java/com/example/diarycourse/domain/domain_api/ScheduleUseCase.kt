package com.example.diarycourse.domain.domain_api

import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface ScheduleUseCase {
    suspend fun insert(item: ScheduleItem): Resource

    suspend fun getAll(): Flow<List<ScheduleItem>>

    suspend fun deleteById(itemId: Int): Resource

    suspend fun deleteAll()

    suspend fun update(item: ScheduleItem): Resource
}