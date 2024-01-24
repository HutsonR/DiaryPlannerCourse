package com.example.diarycourse.data.database

import android.provider.ContactsContract.CommonDataKinds.Note
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.diarycourse.data.models.NoteItemDto
import kotlinx.coroutines.flow.Flow
import com.example.diarycourse.data.models.ScheduleItemDto

@Dao
interface NoteItemDao {
    @Insert
    suspend fun insert(item: NoteItemDto)

    @Query("SELECT * FROM note_items WHERE date = :date")
    fun getNote(date: String): NoteItemDto

    @Update
    fun update(item: NoteItemDto)
}
