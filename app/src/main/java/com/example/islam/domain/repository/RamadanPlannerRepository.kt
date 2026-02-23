package com.example.islam.domain.repository

import com.example.islam.domain.model.IbadahDayStatus
import com.example.islam.domain.model.PrayerDayPlan
import com.example.islam.domain.model.PrayerType
import kotlinx.coroutines.flow.Flow

interface RamadanPlannerRepository {
    fun observeMonth(
        city: String,
        country: String,
        month: Int,
        year: Int,
        method: Int,
        school: Int
    ): Flow<List<PrayerDayPlan>>

    fun observeIbadahProgress(month: Int, year: Int): Flow<List<IbadahDayStatus>>

    suspend fun setFastStatus(dateIso: String, done: Boolean)

    suspend fun setPrayerCompletion(dateIso: String, prayerType: PrayerType, done: Boolean)
}
