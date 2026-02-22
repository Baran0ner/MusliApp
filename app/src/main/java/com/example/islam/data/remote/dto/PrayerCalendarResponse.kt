package com.example.islam.data.remote.dto

/**
 * /v1/calendarByCity yanıtı.
 *
 * data alanı bir **liste** döner — her eleman ayın bir gününe karşılık gelir.
 * Her eleman, tekil sorgunun PrayerData yapısıyla aynıdır, tekrar kullanılır.
 */
data class PrayerCalendarResponse(
    val code: Int,
    val status: String,
    val data: List<PrayerData>   // 28 / 29 / 30 / 31 eleman
)
