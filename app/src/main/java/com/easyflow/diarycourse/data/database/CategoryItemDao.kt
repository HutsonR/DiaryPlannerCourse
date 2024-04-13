package com.easyflow.diarycourse.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.easyflow.diarycourse.data.models.CategoryItemDto

@Dao
interface CategoryItemDao {
    @Insert
    suspend fun insert(item: CategoryItemDto)

    @Query("SELECT * FROM schedule_category ORDER BY id DESC")
    fun getAll(): List<CategoryItemDto>

    @Query("DELETE FROM schedule_category WHERE id = :itemId")
    suspend fun deleteById(itemId: Int)

    @Update
    fun update(item: CategoryItemDto)
}
