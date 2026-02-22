package com.example.islam.data.local.dao

import androidx.room.*
import com.example.islam.data.local.entity.PrayerHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: PrayerHistoryEntity)

    /** Son 7 günün kayıtlarını tarihe göre sıralı döndürür (Flow). */
    @Query("SELECT * FROM prayer_history WHERE date >= :sevenDaysAgo ORDER BY date ASC")
    fun getLast7DaysHistory(sevenDaysAgo: String): Flow<List<PrayerHistoryEntity>>

    @Query("SELECT * FROM prayer_history WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): PrayerHistoryEntity?
}
