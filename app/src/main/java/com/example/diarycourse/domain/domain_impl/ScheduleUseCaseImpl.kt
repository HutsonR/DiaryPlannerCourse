package com.example.diarycourse.domain.domain_impl

import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.data.repository_api.ScheduleRepository
import com.example.diarycourse.domain.domain_api.ScheduleUseCase
import com.example.diarycourse.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScheduleUseCaseImpl @Inject constructor (
    private val scheduleRepository: ScheduleRepository
): ScheduleUseCase {
    override suspend fun insert(item: ScheduleItem): Resource {
        return if (item.text.isEmpty() || item.date.isEmpty() || item.startTime.isEmpty())
            Resource.Empty.Failed
        else {
            scheduleRepository.insert(item)
            Resource.Success
        }
    }

    override suspend fun getAll(): Flow<List<ScheduleItem>> {
        return scheduleRepository.getAll()
    }

    override suspend fun deleteById(itemId: Int): Resource {
        return if (itemId == null)
            Resource.Empty.Failed
        else {
            scheduleRepository.deleteById(itemId)
            Resource.Success
        }
    }

    override suspend fun deleteAll() {
        return scheduleRepository.deleteAll()
    }

    override suspend fun update(item: ScheduleItem): Resource {
        return if (item == null)
            Resource.Empty.Failed
        else {
            scheduleRepository.update(item)
            Resource.Success
        }
    }

}