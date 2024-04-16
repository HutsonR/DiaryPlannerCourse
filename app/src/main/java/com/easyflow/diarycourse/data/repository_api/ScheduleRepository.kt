package com.easyflow.diarycourse.data.repository_api

import com.easyflow.diarycourse.domain.models.ScheduleItem

interface ScheduleRepository {
    suspend fun insert(item: ScheduleItem)

    suspend fun getAll(): List<ScheduleItem>

    suspend fun getByDate(date: String): List<ScheduleItem>

    suspend fun deleteById(itemId: Int)

    suspend fun deleteAll()

    suspend fun update(item: ScheduleItem)
}