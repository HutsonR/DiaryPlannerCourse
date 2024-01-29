package com.easyflow.diarycourse.data.repository_api

import com.easyflow.diarycourse.domain.models.NoteItem
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun insert(item: NoteItem)

    fun getNote(date: String): NoteItem?

    suspend fun getAll(): Flow<List<NoteItem>>

    suspend fun deleteById(itemId: Int)

    suspend fun update(item: NoteItem)
}