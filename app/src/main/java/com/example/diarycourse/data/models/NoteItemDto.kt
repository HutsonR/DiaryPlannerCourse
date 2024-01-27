package com.example.diarycourse.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_items")
data class NoteItemDto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val date: String
)