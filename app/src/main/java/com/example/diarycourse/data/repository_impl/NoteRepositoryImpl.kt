package com.example.diarycourse.data.repository_impl

import android.provider.ContactsContract.CommonDataKinds.Note
import android.util.Log
import com.example.diarycourse.data.database.NoteItemDao
import com.example.diarycourse.data.mapper.Mapper
import com.example.diarycourse.data.models.NoteItemDto
import com.example.diarycourse.data.models.ScheduleItemDto
import com.example.diarycourse.data.repository_api.NoteRepository
import com.example.diarycourse.domain.models.NoteItem
import com.example.diarycourse.domain.models.ScheduleItem
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