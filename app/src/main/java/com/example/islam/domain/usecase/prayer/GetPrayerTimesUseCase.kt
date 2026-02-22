package com.example.islam.domain.usecase.prayer

import com.example.islam.core.util.Resource
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.repository.PrayerTimeRepository
import javax.inject.Inject

class GetPrayerTimesUseCase @Inject constructor(
    private val repository: PrayerTimeRepository
) {
    suspend operator fun invoke(
        city: String,
        country: String,
        method: Int = 13,
        school: Int = 0,
        date: String? = null
    ): Resource<PrayerTime> = repository.getPrayerTimes(city, country, method, school, date)
}
