package com.easyflow.diarycourse.domain.domain_impl

import com.easyflow.diarycourse.domain.models.ScheduleItem
import com.easyflow.diarycourse.data.repository_api.ScheduleRepository
import com.easyflow.diarycourse.domain.domain_api.ScheduleUseCase
import com.easyflow.diarycourse.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScheduleUseCaseImpl @Inject constructor (
    private val scheduleRepository: ScheduleRepository
): ScheduleUseCase {
    override suspend fun insert(item: ScheduleItem): Resource{
        return if (item.text.isEmpty() || item.date.isEmpty() || item.startTime.isEmpty())
            Resource.Failed(Exception("Fields can not be empty"))
        else {
            Resource.Success(scheduleRepository.insert(item))
        }
    }

    override suspend fun getAll(): List<ScheduleItem> {
        return scheduleRepository.getAll()
    }

    override suspend fun deleteById(itemId: Int): Resource =
        Resource.Success(scheduleRepository.deleteById(itemId))

    override suspend fun deleteAll() =
        scheduleRepository.deleteAll()

    override suspend fun update(item: ScheduleItem): Resource =
        Resource.Success(scheduleRepository.update(item))

}