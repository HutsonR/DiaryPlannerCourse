package com.easyflow.diarycourse.data.repository_impl

import com.easyflow.diarycourse.data.database.NoteItemDao
import com.easyflow.diarycourse.data.mapper.Mapper
import com.easyflow.diarycourse.data.models.NoteItemDto
import com.easyflow.diarycourse.data.repository_api.NoteRepository
import com.easyflow.diarycourse.domain.models.NoteItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor (
    private val noteItemDao: NoteItemDao
): NoteRepository {

    private val mapper = Mapper
    override suspend fun insert(item: NoteItem) {
        val noteItemDto = mapper.mapFrom(item)
        return noteItemDao.insert(noteItemDto)
    }

    override fun getNote(date: String): NoteItem? {
        val noteItemDto: NoteItemDto? = noteItemDao.getNote(date)
        return if (noteItemDto != null) {
            mapper.mapTo(noteItemDto)
        } else null
    }

    override suspend fun getAll(): Flow<List<NoteItem>> {
        val noteItemDtos: Flow<List<NoteItemDto>> = noteItemDao.getAll()
        val noteItems: Flow<List<NoteItem>> = noteItemDtos.map { dtoList ->
            dtoList.map { mapper.mapTo(it) }
        }
        return noteItems
    }

    override suspend fun deleteById(itemId: Int) {
        return noteItemDao.deleteById(itemId)
    }

    override suspend fun update(item: NoteItem) {
        val noteItemDto = mapper.mapFrom(item)
        return noteItemDao.update(noteItemDto)
    }

}