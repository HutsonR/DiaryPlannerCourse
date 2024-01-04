package com.example.diarycourse.domain.domain_impl

import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.data.repository_api.Repository
import com.example.diarycourse.domain.domain_api.UseCase
import com.example.diarycourse.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UseCaseImpl @Inject constructor (
    private val repository: Repository
): UseCase {
    override suspend fun insert(item: ScheduleItem): Resource {
        return if (item.text.isEmpty() || item.description.isEmpty() || item.startTime.isEmpty())
            Resource.Empty.Failed
        else {
            repository.insert(item)
            Resource.Success
        }
    }

    override suspend fun getAll(): Flow<List<ScheduleItem>> {
        return repository.getAll()
    }

    override suspend fun deleteById(itemId: Int): Resource {
        return if (itemId == null)
            Resource.Empty.Failed
        else {
            repository.deleteById(itemId)
            Resource.Success
        }
    }

    override suspend fun deleteAll() {
        return repository.deleteAll()
    }

    override suspend fun update(item: ScheduleItem) {
        return repository.update(item)
    }

}