package com.example.diarycourse.domain.domain_api

import com.example.diarycourse.data.models.NoteItemDto
import com.example.diarycourse.domain.models.NoteItem
import com.example.diarycourse.domain.models.ScheduleItem
import com.example.diarycourse.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface NoteUseCase {
    suspend fun insert(item: NoteItem): Resource

    fun getNote(date: String): NoteItem?

    suspend fun update(item: NoteItem): Resource
}