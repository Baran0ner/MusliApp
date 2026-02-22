package com.example.islam.data.local.dao

import androidx.room.*
import com.example.islam.data.local.entity.PrayerTimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerTimeDao {

    // ── Tekil okuma ──────────────────────────────────────────────────────────

    @Query("SELECT * FROM prayer_times WHERE date = :date AND city = :city LIMIT 1")
    suspend fun getPrayerTimeByDate(date: String, city: String): PrayerTimeEntity?

    // ── Ay cache kontrolü ────────────────────────────────────────────────────

    /**
     * O aya ait kaç kayıt var?
     * ≥ 28 ise ay tamamen önbellekte sayılır, API'ye gidilmez.
     */
    @Query("SELECT COUNT(*) FROM prayer_times WHERE month = :month AND year = :year AND city = :city")
    suspend fun countForMonth(month: Int, year: Int, city: String): Int

    // ── Yazma ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTime(entity: PrayerTimeEntity)

    /** Tüm ay verilerini tek seferde yaz (calendarByCity yanıtı). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PrayerTimeEntity>)

    // ── Flow akışı ───────────────────────────────────────────────────────────

    @Query("SELECT * FROM prayer_times WHERE city = :city ORDER BY date DESC")
    fun getPrayerTimesFlow(city: String): Flow<List<PrayerTimeEntity>>

    // ── Temizlik ─────────────────────────────────────────────────────────────

    @Query("DELETE FROM prayer_times WHERE date < :date")
    suspend fun deleteOldPrayerTimes(date: String)

    /**
     * Geçmiş aylara ait verileri siler.
     * Örn: year=2026, month=2 → Ocak 2026 ve öncesi silinir.
     */
    @Query("""
        DELETE FROM prayer_times
        WHERE (year < :year) OR (year = :year AND month < :month)
    """)
    suspend fun clearOldMonths(year: Int, month: Int)
}

