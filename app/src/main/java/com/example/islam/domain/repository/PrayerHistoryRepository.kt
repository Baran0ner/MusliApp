package com.example.islam.domain.repository

import com.example.islam.domain.model.PrayerType
import com.example.islam.domain.model.WeekDay
import kotlinx.coroutines.flow.Flow

interface PrayerHistoryRepository {
    fun getLast7Days(): Flow<List<WeekDay>>
    suspend fun togglePrayerStatus(date: String, prayerType: PrayerType)
}
