package com.example.diarycourse.data.repository_api

import com.example.diarycourse.data.models.NoteItemDto
import com.example.diarycourse.domain.models.NoteItem
import com.example.diarycourse.domain.models.ScheduleItem
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun insert(item: NoteItem)

    fun getNote(date: String): NoteItem?

    suspend fun update(item: NoteItem)
}