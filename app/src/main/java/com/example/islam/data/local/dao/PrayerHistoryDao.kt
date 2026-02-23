package com.example.islam.data.local.dao

import androidx.room.*
import com.example.islam.data.local.entity.PrayerHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: PrayerHistoryEntity)

    /** Belirli tarih aralığındaki kayıtları tarihe göre sıralı döndürür (Flow). */
    @Query("SELECT * FROM prayer_history WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getLast7DaysHistory(startDate: String, endDate: String): Flow<List<PrayerHistoryEntity>>

    @Query("SELECT * FROM prayer_history WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): PrayerHistoryEntity?
}
