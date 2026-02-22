package com.example.islam.data.local.dao

import androidx.room.*
import com.example.islam.data.local.entity.DhikrEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrDao {

    @Query("SELECT * FROM dhikrs ORDER BY id ASC")
    fun getAllDhikrs(): Flow<List<DhikrEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDhikr(dhikr: DhikrEntity): Long

    @Update
    suspend fun updateDhikr(dhikr: DhikrEntity)

    @Delete
    suspend fun deleteDhikr(dhikr: DhikrEntity)

    @Query("UPDATE dhikrs SET count = count + 1 WHERE id = :id")
    suspend fun incrementCount(id: Long)

    @Query("UPDATE dhikrs SET count = 0 WHERE id = :id")
    suspend fun resetCount(id: Long)

    @Query("UPDATE dhikrs SET count = 0")
    suspend fun resetAllCounts()

    @Query("SELECT COUNT(*) FROM dhikrs")
    suspend fun getCount(): Int
}
