package com.easyflow.diarycourse.data.repository_impl

import com.easyflow.diarycourse.data.database.ScheduleItemDao
import com.easyflow.diarycourse.data.mapper.Mapper
import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.data.models.ScheduleItemDto
import com.easyflow.diarycourse.data.repository_api.ScheduleRepository
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor (
    private val scheduleItemDao: ScheduleItemDao
): ScheduleRepository {

    private val mapper = Mapper
    override suspend fun insert(item: ScheduleItem) {
        val scheduleItemDto = mapper.mapFrom(item)
        return scheduleItemDao.insert(scheduleItemDto)
    }

    override suspend fun getAll(): List<ScheduleItem> {
        val scheduleItemDtos: List<ScheduleItemDto> = scheduleItemDao.getAll()
        return scheduleItemDtos.map { mapper.mapTo(it) }
    }

    override suspend fun deleteById(itemId: Int) {
        return scheduleItemDao.deleteById(itemId)
    }

    override suspend fun deleteAll() {
        return scheduleItemDao.deleteAll()
    }

    override suspend fun update(item: ScheduleItem) {
        val scheduleItemDto = mapper.mapFrom(item)
        return scheduleItemDao.update(scheduleItemDto)
    }

}