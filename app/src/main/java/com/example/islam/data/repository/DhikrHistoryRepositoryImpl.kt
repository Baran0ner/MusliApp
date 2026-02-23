package com.example.islam.data.repository

import com.example.islam.data.local.dao.DhikrHistoryDao
import com.example.islam.data.local.entity.DhikrHistoryEntity
import com.example.islam.domain.model.DhikrDay
import com.example.islam.domain.model.DhikrRecord
import com.example.islam.domain.repository.DhikrHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DhikrHistoryRepositoryImpl @Inject constructor(
    private val dao: DhikrHistoryDao
) : DhikrHistoryRepository {

    private val turkishDayNames = mapOf(
        Calendar.MONDAY to "Pzt",
        Calendar.TUESDAY to "Sal",
        Calendar.WEDNESDAY to "Çar",
        Calendar.THURSDAY to "Per",
        Calendar.FRIDAY to "Cum",
        Calendar.SATURDAY to "Cmt",
        Calendar.SUNDAY to "Paz"
    )

    private fun last7Dates(): List<Pair<String, String>> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return (6 downTo 0).map { daysAgo ->
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
            val date = sdf.format(cal.time)
            val shortName = turkishDayNames[cal.get(Calendar.DAY_OF_WEEK)] ?: ""
            date to shortName
        }
    }

    override suspend fun saveRecord(date: String, dhikrName: String, count: Int) {
        dao.insert(DhikrHistoryEntity(date = date, dhikrName = dhikrName, count = count))
    }

    override fun getLast7Days(): Flow<List<DhikrDay>> {
        val days = last7Dates()
        val fromDate = days.first().first
        return dao.getHistoryFromDate(fromDate).map { entities ->
            val byDate = entities.groupBy { it.date }
            days.map { (date, shortName) ->
                val list = byDate[date]?.map { DhikrRecord(it.dhikrName, it.count) } ?: emptyList()
                DhikrDay(date = date, shortName = shortName, records = list)
            }
        }
    }

    override suspend fun getRecordsByDate(date: String): List<DhikrRecord> {
        return dao.getByDate(date).map { DhikrRecord(it.dhikrName, it.count) }
    }
}
