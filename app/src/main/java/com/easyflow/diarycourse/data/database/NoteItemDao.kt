package com.easyflow.diarycourse.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.easyflow.diarycourse.data.models.NoteItemDto
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteItemDao {
    @Insert
    suspend fun insert(item: NoteItemDto)

    @Query("SELECT * FROM note_items WHERE date = :date")
    fun getNote(date: String): NoteItemDto

    @Query("SELECT * FROM note_items ORDER BY id DESC")
    fun getAll(): Flow<List<NoteItemDto>>

    @Query("DELETE FROM note_items WHERE id = :itemId")
    suspend fun deleteById(itemId: Int)

    @Update
    fun update(item: NoteItemDto)
}
