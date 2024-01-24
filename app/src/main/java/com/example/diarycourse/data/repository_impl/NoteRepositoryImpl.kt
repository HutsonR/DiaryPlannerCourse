package com.example.diarycourse.data.repository_impl

import com.example.diarycourse.data.database.NoteItemDao
import com.example.diarycourse.data.mapper.Mapper
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
        val noteItemDto = mapper.mapTo(item)
        return noteItemDao.insert(noteItemDto)
    }

    override fun getNote(date: String): NoteItem {
        val noteItemDto = noteItemDao.getNote(date)
        return mapper.mapTo(noteItemDto)
    }

    override suspend fun update(item: NoteItem) {
        val noteItemDto = mapper.mapTo(item)
        return noteItemDao.update(noteItemDto)
    }

}