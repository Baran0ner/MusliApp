package com.example.islam.data.repository

import com.example.islam.data.local.dao.IbadahDayDao
import com.example.islam.data.local.dao.PrayerHistoryDao
import com.example.islam.data.local.dao.PrayerTimeDao
import com.example.islam.data.local.entity.PrayerHistoryEntity
import com.example.islam.domain.model.IbadahDayStatus
import com.example.islam.domain.model.PrayerDayPlan
import com.example.islam.domain.model.PrayerType
import com.example.islam.domain.repository.RamadanPlannerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class RamadanPlannerRepositoryImpl @Inject constructor(
    private val prayerTimeDao: PrayerTimeDao,
    private val prayerHistoryDao: PrayerHistoryDao,
    private val ibadahDayDao: IbadahDayDao
) : RamadanPlannerRepository {

    private val apiDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    override fun observeMonth(
        city: String,
        country: String,
        month: Int,
        year: Int,
        method: Int,
        school: Int
    ): Flow<List<PrayerDayPlan>> {
        return prayerTimeDao.observeMonth(
            month = month,
            year = year,
            city = city,
            country = country,
            method = method,
            school = school
        ).map { entities ->
            entities.mapNotNull { entity ->
                val dateIso = runCatching {
                    LocalDate.parse(entity.date, apiDateFormatter).toString()
                }.getOrNull() ?: return@mapNotNull null
                PrayerDayPlan(
                    dateIso = dateIso,
                    hijriDate = entity.hijriDate,
                    fajr = entity.fajr,
                    dhuhr = entity.dhuhr,
                    asr = entity.asr,
                    maghrib = entity.maghrib,
                    isha = entity.isha
                )
            }
        }
    }

    override fun observeIbadahProgress(month: Int, year: Int): Flow<List<IbadahDayStatus>> {
        val yearMonth = YearMonth.of(year, month)
        val start = yearMonth.atDay(1).toString()
        val end = yearMonth.atEndOfMonth().toString()

        return combine(
            prayerHistoryDao.getLast7DaysHistory(startDate = start, endDate = end),
            ibadahDayDao.observeBetween(startDate = start, endDate = end)
        ) { prayerHistory, ibadahHistory ->
            val prayerByDate = prayerHistory.associateBy { it.date }
            val ibadahByDate = ibadahHistory.associateBy { it.date }

            (1..yearMonth.lengthOfMonth()).map { day ->
                val dateIso = yearMonth.atDay(day).toString()
                val prayer = prayerByDate[dateIso]
                val ibadah = ibadahByDate[dateIso]
                IbadahDayStatus(
                    dateIso = dateIso,
                    fastDone = ibadah?.fastDone ?: false,
                    fajrDone = prayer?.isFajrPrayed ?: false,
                    dhuhrDone = prayer?.isDhuhrPrayed ?: false,
                    asrDone = prayer?.isAsrPrayed ?: false,
                    maghribDone = prayer?.isMaghribPrayed ?: false,
                    ishaDone = prayer?.isIshaPrayed ?: false,
                    prayedCount = prayer?.prayedCount() ?: 0,
                    dhikrCount = ibadah?.dhikrCount ?: 0,
                    quranMinutes = ibadah?.quranMinutes ?: 0
                )
            }
        }
    }

    override suspend fun setFastStatus(dateIso: String, done: Boolean) {
        val current = ibadahDayDao.getByDate(dateIso)
        if (current == null) {
            ibadahDayDao.upsert(
                com.example.islam.data.local.entity.IbadahDayEntity(
                    date = dateIso,
                    fastDone = done
                )
            )
        } else {
            ibadahDayDao.upsert(current.copy(fastDone = done))
        }
    }

    override suspend fun setPrayerCompletion(dateIso: String, prayerType: PrayerType, done: Boolean) {
        val current = prayerHistoryDao.getByDate(dateIso) ?: PrayerHistoryEntity(date = dateIso)
        val updated = when (prayerType) {
            PrayerType.FAJR -> current.copy(isFajrPrayed = done)
            PrayerType.DHUHR -> current.copy(isDhuhrPrayed = done)
            PrayerType.ASR -> current.copy(isAsrPrayed = done)
            PrayerType.MAGHRIB -> current.copy(isMaghribPrayed = done)
            PrayerType.ISHA -> current.copy(isIshaPrayed = done)
        }
        prayerHistoryDao.insertOrUpdate(updated)
    }

    private fun com.example.islam.data.local.entity.PrayerHistoryEntity.prayedCount(): Int {
        return listOf(
            isFajrPrayed,
            isDhuhrPrayed,
            isAsrPrayed,
            isMaghribPrayed,
            isIshaPrayed
        ).count { it }
    }
}
