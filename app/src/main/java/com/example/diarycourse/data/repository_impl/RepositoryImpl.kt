package com.example.diarycourse.data.repository_impl

import com.example.diarycourse.data.database.ScheduleItemDao
import com.example.diarycourse.data.models.ScheduleItem
import com.example.diarycourse.data.repository_api.Repository
import javax.inject.Inject

class RepositoryImpl @Inject constructor (
    private val scheduleItemDao: ScheduleItemDao
): Repository {
    override suspend fun insert(historyItem: ScheduleItem) {
        return scheduleItemDao.insert(historyItem)
    }

    override suspend fun getAll(): List<ScheduleItem> {
        return scheduleItemDao.getAll()
    }

    override suspend fun deleteById(itemId: Int) {
        return scheduleItemDao.deleteById(itemId)
    }

    override suspend fun deleteAll() {
        return scheduleItemDao.deleteAll()
    }

}