package com.easyflow.diarycourse.data.repository_impl

import com.easyflow.diarycourse.data.database.ScheduleItemDao
import com.easyflow.diarycourse.data.mapper.Mapper
import com.easyflow.diarycourse.data.models.ScheduleItemDto
import com.easyflow.diarycourse.data.repository_api.ScheduleRepository
import com.easyflow.diarycourse.domain.models.ScheduleItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor (
    private val scheduleItemDao: ScheduleItemDao
): ScheduleRepository {

    private val mapper = Mapper
    override suspend fun insert(item: ScheduleItem) = withContext(Dispatchers.IO) {
        val scheduleItemDto = mapper.mapToScheduleItemDto(item)
        scheduleItemDao.insert(scheduleItemDto)
    }

    override suspend fun getAll(): List<ScheduleItem> = withContext(Dispatchers.IO) {
        val scheduleItemDtos: List<ScheduleItemDto> = scheduleItemDao.getAll()
        scheduleItemDtos.map { mapper.mapToScheduleItem(it) }
    }

    override suspend fun getByDate(date: String): List<ScheduleItem> = withContext(Dispatchers.IO) {
        val scheduleItemDtos: List<ScheduleItemDto> = scheduleItemDao.getByDate(date)
        scheduleItemDtos.map { mapper.mapToScheduleItem(it) }
    }

    override suspend fun deleteById(itemId: Int) = withContext(Dispatchers.IO) {
        scheduleItemDao.deleteById(itemId)
    }

    override suspend fun deleteAll() = withContext(Dispatchers.IO) {
        scheduleItemDao.deleteAll()
    }

    override suspend fun update(item: ScheduleItem) = withContext(Dispatchers.IO) {
        val scheduleItemDto = mapper.mapToScheduleItemDto(item)
        scheduleItemDao.update(scheduleItemDto)
    }

}