package com.example.diarycourse.domain.domain_impl

import com.example.diarycourse.data.models.ScheduleItem
import com.example.diarycourse.data.repository_api.Repository
import com.example.diarycourse.domain.domain_api.UseCase
import javax.inject.Inject

class UseCaseImpl @Inject constructor (
    private val repository: Repository
): UseCase {
    override suspend fun insert(historyItem: ScheduleItem) {
        return repository.insert(historyItem)
    }

    override suspend fun getAll(): List<ScheduleItem> {
        return repository.getAll()
    }

    override suspend fun deleteById(itemId: Int) {
        return repository.deleteById(itemId)
    }

    override suspend fun deleteAll() {
        return repository.deleteAll()
    }

}