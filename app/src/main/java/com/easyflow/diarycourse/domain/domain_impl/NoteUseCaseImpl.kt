package com.easyflow.diarycourse.domain.domain_impl

import com.easyflow.diarycourse.data.repository_api.NoteRepository
import com.easyflow.diarycourse.domain.domain_api.NoteUseCase
import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteUseCaseImpl @Inject constructor (
    private val noteRepository: NoteRepository
): NoteUseCase {
    override suspend fun insert(item: NoteItem): Resource {
        return if (item.text.isEmpty())
            Resource.Empty.Failed
        else {
            noteRepository.insert(item)
            Resource.Success
        }
    }

    override fun getNote(date: String): NoteItem? {
        return noteRepository.getNote(date)
    }

    override suspend fun getAll(): List<NoteItem> {
        return noteRepository.getAll()
    }

    override suspend fun deleteById(itemId: Int): Resource {
        return if (itemId == null)
            Resource.Empty.Failed
        else {
            noteRepository.deleteById(itemId)
            Resource.Success
        }
    }

    override suspend fun update(item: NoteItem): Resource {
        return if (item == null)
            Resource.Empty.Failed
        else {
            noteRepository.update(item)
            Resource.Success
        }
    }

}