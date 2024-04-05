package com.easyflow.diarycourse.domain.domain_api

import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.domain.util.Resource

interface ScheduleUseCase {
    suspend fun insert(item: ScheduleItem): Resource

    suspend fun getAll(): List<ScheduleItem>

    suspend fun getByDate(date: String): List<ScheduleItem>

    suspend fun deleteById(itemId: Int): Resource

    suspend fun deleteAll()

    suspend fun update(item: ScheduleItem): Resource
}