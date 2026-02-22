package com.example.islam.data.mapper

import com.example.islam.core.util.DateUtil.cleanTime
import com.example.islam.data.local.entity.PrayerTimeEntity
import com.example.islam.data.remote.dto.PrayerData
import com.example.islam.data.remote.dto.PrayerTimeResponse
import com.example.islam.domain.model.PrayerTime

// ── Entity → Domain ───────────────────────────────────────────────────────────

fun PrayerTimeEntity.toDomain() = PrayerTime(
    id = id,
    date = date,
    imsak = imsak,
    fajr = fajr,
    sunrise = sunrise,
    dhuhr = dhuhr,
    asr = asr,
    maghrib = maghrib,
    isha = isha,
    hijriDate = hijriDate,
    city = city,
    country = country
)

// ── Domain → Entity ───────────────────────────────────────────────────────────

fun PrayerTime.toEntity(): PrayerTimeEntity {
    // date formatı "19-02-2026" → parts[2]=2026, parts[1]=2
    val parts = date.split("-")
    val month = if (parts.size >= 3) parts[1].toIntOrNull() ?: 0 else 0
    val year  = if (parts.size >= 3) parts[2].toIntOrNull() ?: 0 else 0
    return PrayerTimeEntity(
        id = id,
        date = date,
        month = month,
        year = year,
        imsak = imsak,
        fajr = fajr,
        sunrise = sunrise,
        dhuhr = dhuhr,
        asr = asr,
        maghrib = maghrib,
        isha = isha,
        hijriDate = hijriDate,
        city = city,
        country = country
    )
}

// ── DTO → Entity (tekil gün) ─────────────────────────────────────────────────

fun PrayerTimeResponse.toEntity(city: String, country: String): PrayerTimeEntity =
    data.toEntity(city, country)

// ── DTO → Entity (PrayerData — hem tekil hem de takvim yanıtı için) ──────────

fun PrayerData.toEntity(city: String, country: String): PrayerTimeEntity {
    val t = timings
    val hijri = date.hijri
    val greg  = date.gregorian
    return PrayerTimeEntity(
        date      = greg.date,                              // "19-02-2026"
        month     = greg.month.number,                     // 2
        year      = greg.year.toIntOrNull() ?: 0,          // 2026
        imsak     = t.imsak.cleanTime(),
        fajr      = t.fajr.cleanTime(),
        sunrise   = t.sunrise.cleanTime(),
        dhuhr     = t.dhuhr.cleanTime(),
        asr       = t.asr.cleanTime(),
        maghrib   = t.maghrib.cleanTime(),
        isha      = t.isha.cleanTime(),
        hijriDate = "${hijri.day} ${hijri.month.ar} ${hijri.year}",
        city      = city,
        country   = country
    )
}

