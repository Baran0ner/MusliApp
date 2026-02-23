package com.example.islam.data.repository

import com.example.islam.data.local.dao.PrayerHistoryDao
import com.example.islam.data.local.entity.PrayerHistoryEntity
import com.example.islam.domain.model.PrayerHistory
import com.example.islam.domain.model.PrayerType
import com.example.islam.domain.model.WeekDay
import com.example.islam.domain.repository.PrayerHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class PrayerHistoryRepositoryImpl @Inject constructor(
    private val dao: PrayerHistoryDao
) : PrayerHistoryRepository {

    internal var currentDateProvider: () -> LocalDate = { LocalDate.now() }
    private val toggleMutex = Mutex()

    override fun getLast7Days(): Flow<List<WeekDay>> {
        val days = currentWeekDates(currentDateProvider())
        val startDate = days.first().first
        val endDate = days.last().first

        return dao.getLast7DaysHistory(startDate, endDate).map { entities ->
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
        toggleMutex.withLock {
            val current = dao.getByDate(date) ?: PrayerHistoryEntity(date = date)
            val updated = when (prayerType) {
                PrayerType.FAJR -> current.copy(isFajrPrayed = !current.isFajrPrayed)
                PrayerType.DHUHR -> current.copy(isDhuhrPrayed = !current.isDhuhrPrayed)
                PrayerType.ASR -> current.copy(isAsrPrayed = !current.isAsrPrayed)
                PrayerType.MAGHRIB -> current.copy(isMaghribPrayed = !current.isMaghribPrayed)
                PrayerType.ISHA -> current.copy(isIshaPrayed = !current.isIshaPrayed)
            }
            dao.insertOrUpdate(updated)
        }
    }

    /** Geçerli haftayı (Pzt..Paz) döndürür. */
    private fun currentWeekDates(today: LocalDate): List<Pair<String, String>> {
        val weekStart = today.with(DayOfWeek.MONDAY)
        return (0L..6L).map { offset ->
            val date = weekStart.plusDays(offset)
            date.toString() to turkishShortName(date.dayOfWeek)
        }
    }

    private fun turkishShortName(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Pzt"
        DayOfWeek.TUESDAY -> "Sal"
        DayOfWeek.WEDNESDAY -> "Çar"
        DayOfWeek.THURSDAY -> "Per"
        DayOfWeek.FRIDAY -> "Cum"
        DayOfWeek.SATURDAY -> "Cmt"
        DayOfWeek.SUNDAY -> "Paz"
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
