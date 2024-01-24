package com.example.diarycourse.domain.domain_impl

import com.example.diarycourse.data.repository_api.NoteRepository
import com.example.diarycourse.domain.domain_api.NoteUseCase
import com.example.diarycourse.domain.models.NoteItem
import com.example.diarycourse.domain.util.Resource
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

    override suspend fun update(item: NoteItem): Resource {
        return if (item == null)
            Resource.Empty.Failed
        else {
            noteRepository.update(item)
            Resource.Success
        }
    }

}