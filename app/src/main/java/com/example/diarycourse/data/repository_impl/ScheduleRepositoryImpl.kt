package com.example.diarycourse.data.repository_impl

import com.example.diarycourse.data.database.ScheduleItemDao
import com.example.diarycourse.data.mapper.Mapper
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.data.models.ScheduleItemDto
import com.example.diarycourse.data.repository_api.ScheduleRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScheduleRepositoryImpl @Inject constructor (
    private val scheduleItemDao: ScheduleItemDao
): ScheduleRepository {

    private val mapper = Mapper
    override suspend fun insert(item: ScheduleItem) {
        val scheduleItemDto = mapper.mapFrom(item)
        return scheduleItemDao.insert(scheduleItemDto)
    }

    override suspend fun getAll(): Flow<List<ScheduleItem>> {
        val scheduleItemDtos: Flow<List<ScheduleItemDto>> = scheduleItemDao.getAll()
        val scheduleItems: Flow<List<ScheduleItem>> = scheduleItemDtos.map { dtoList ->
            dtoList.map { mapper.mapTo(it) }
        }
        return scheduleItems
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