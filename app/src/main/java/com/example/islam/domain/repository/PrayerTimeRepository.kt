package com.example.islam.domain.repository

import com.example.islam.core.util.Resource
import com.example.islam.domain.model.PrayerTime
import kotlinx.coroutines.flow.Flow

interface PrayerTimeRepository {

    /** Fetches prayer times for today; tries cache first, falls back to network */
    suspend fun getPrayerTimes(
        city: String,
        country: String,
        method: Int = 13,
        school: Int = 0,
        date: String? = null
    ): Resource<PrayerTime>

    /** Reactive stream of cached prayer times for a city */
    fun getPrayerTimesFlow(city: String): Flow<List<PrayerTime>>

    /** Removes prayer time records older than the given date string "dd-MM-yyyy" */
    suspend fun deleteOldPrayerTimes(beforeDate: String)
}
