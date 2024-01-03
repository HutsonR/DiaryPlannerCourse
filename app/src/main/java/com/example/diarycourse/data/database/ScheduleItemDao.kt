package com.example.diarycourse.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.diarycourse.data.models.ScheduleItem

@Dao
interface ScheduleItemDao {
    @Insert
    suspend fun insert(historyItem: ScheduleItem)

    @Query("SELECT * FROM schedule_items ORDER BY id DESC")
    suspend fun getAll(): List<ScheduleItem>

    @Query("DELETE FROM schedule_items WHERE id = :itemId")
    suspend fun deleteById(itemId: Int)

    @Query("DELETE FROM schedule_items")
    suspend fun deleteAll()
}
