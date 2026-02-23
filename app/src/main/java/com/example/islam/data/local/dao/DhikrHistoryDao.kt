package com.example.islam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.islam.data.local.entity.DhikrHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrHistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: DhikrHistoryEntity): Long

    @Query("SELECT * FROM dhikr_history WHERE date >= :fromDate ORDER BY date DESC, id DESC")
    fun getHistoryFromDate(fromDate: String): Flow<List<DhikrHistoryEntity>>

    @Query("SELECT * FROM dhikr_history WHERE date = :date ORDER BY id DESC")
    suspend fun getByDate(date: String): List<DhikrHistoryEntity>
}
