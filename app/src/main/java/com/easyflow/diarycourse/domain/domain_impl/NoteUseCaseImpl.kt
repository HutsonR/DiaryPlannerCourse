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
            Resource.Failed(Exception("Fields can not be empty"))
        else {
            Resource.Success(noteRepository.insert(item))
        }
    }

    override fun getNote(date: String): NoteItem? {
        return noteRepository.getNote(date)
    }

    override suspend fun getAll(): List<NoteItem> {
        return noteRepository.getAll()
    }

    override suspend fun deleteById(itemId: Int): Resource =
        Resource.Success(noteRepository.deleteById(itemId))

    override suspend fun update(item: NoteItem): Resource =
        Resource.Success(noteRepository.update(item))

}