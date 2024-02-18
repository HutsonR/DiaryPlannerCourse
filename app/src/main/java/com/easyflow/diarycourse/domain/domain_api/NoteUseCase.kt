package com.easyflow.diarycourse.domain.domain_api

import com.easyflow.diarycourse.domain.models.NoteItem
import com.easyflow.diarycourse.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface NoteUseCase {
    suspend fun insert(item: NoteItem): Resource

    fun getNote(date: String): NoteItem?

    suspend fun getAll(): List<NoteItem>

    suspend fun deleteById(itemId: Int): Resource

    suspend fun update(item: NoteItem): Resource
}