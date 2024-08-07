package com.easyflow.diarycourse.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.easyflow.diarycourse.data.models.ScheduleItemDto

@Dao
interface ScheduleItemDao {
    @Insert
    suspend fun insert(item: ScheduleItemDto): Long

    @Query("SELECT * FROM schedule_items WHERE id = :itemId")
    suspend fun getById(itemId: Int): ScheduleItemDto?

    @Query("SELECT * FROM schedule_items ORDER BY id DESC")
    fun getAll(): List<ScheduleItemDto>

    @Query("SELECT * FROM schedule_items WHERE date = :date")
    fun getByDate(date: String): List<ScheduleItemDto>

    @Query("DELETE FROM schedule_items WHERE id = :itemId")
    suspend fun deleteById(itemId: Int)

    @Query("DELETE FROM schedule_items")
    suspend fun deleteAll()

    @Update
    fun update(item: ScheduleItemDto)
}
