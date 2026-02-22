package com.example.islam.data.repository

import com.example.islam.data.local.dao.PrayerHistoryDao
import com.example.islam.data.local.entity.PrayerHistoryEntity
import com.example.islam.domain.model.PrayerHistory
import com.example.islam.domain.model.PrayerType
import com.example.islam.domain.model.WeekDay
import com.example.islam.domain.repository.PrayerHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class PrayerHistoryRepositoryImpl @Inject constructor(
    private val dao: PrayerHistoryDao
) : PrayerHistoryRepository {

    private val turkishDayNames = mapOf(
        Calendar.MONDAY    to "Pzt",
        Calendar.TUESDAY   to "Sal",
        Calendar.WEDNESDAY to "Çar",
        Calendar.THURSDAY  to "Per",
        Calendar.FRIDAY    to "Cum",
        Calendar.SATURDAY  to "Cmt",
        Calendar.SUNDAY    to "Paz"
    )

    /** Son 7 günün (bugün dahil) tarih + kısa ad listesi, eskiden yeniye sıralı. */
    private fun last7Dates(): List<Pair<String, String>> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return (6 downTo 0).map { daysAgo ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
            val date      = sdf.format(cal.time)
            val shortName = turkishDayNames[cal.get(Calendar.DAY_OF_WEEK)] ?: ""
            date to shortName
        }
    }

    override fun getLast7Days(): Flow<List<WeekDay>> {
        val days         = last7Dates()
        val sevenDaysAgo = days.first().first

        return dao.getLast7DaysHistory(sevenDaysAgo).map { entities ->
            val byDate = entities.associateBy { it.date }
            days.map { (date, shortName) ->
                WeekDay(
                    date      = date,
                    shortName = shortName,
                    history   = byDate[date]?.toDomain() ?: PrayerHistory(date = date)
                )
            }
        }
    }

    override suspend fun togglePrayerStatus(date: String, prayerType: PrayerType) {
        val current = dao.getByDate(date) ?: PrayerHistoryEntity(date = date)
        val updated = when (prayerType) {
            PrayerType.FAJR    -> current.copy(isFajrPrayed    = !current.isFajrPrayed)
            PrayerType.DHUHR   -> current.copy(isDhuhrPrayed   = !current.isDhuhrPrayed)
            PrayerType.ASR     -> current.copy(isAsrPrayed     = !current.isAsrPrayed)
            PrayerType.MAGHRIB -> current.copy(isMaghribPrayed = !current.isMaghribPrayed)
            PrayerType.ISHA    -> current.copy(isIshaPrayed    = !current.isIshaPrayed)
        }
        dao.insertOrUpdate(updated)
    }

    // ── Entity → Domain ──────────────────────────────────────────────────────
    private fun PrayerHistoryEntity.toDomain() = PrayerHistory(
        date            = date,
        isFajrPrayed    = isFajrPrayed,
        isDhuhrPrayed   = isDhuhrPrayed,
        isAsrPrayed     = isAsrPrayed,
        isMaghribPrayed = isMaghribPrayed,
        isIshaPrayed    = isIshaPrayed
    )
}
