package com.example.islam.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PrayerTimeResponse(
    val code: Int,
    val status: String,
    val data: PrayerData
)

data class PrayerData(
    val timings: TimingsDto,
    val date: DateDto,
    val meta: MetaDto
)

data class TimingsDto(
    @SerializedName("Imsak")   val imsak: String,
    @SerializedName("Fajr")    val fajr: String,
    @SerializedName("Sunrise") val sunrise: String,
    @SerializedName("Dhuhr")   val dhuhr: String,
    @SerializedName("Asr")     val asr: String,
    @SerializedName("Maghrib") val maghrib: String,
    @SerializedName("Isha")    val isha: String
)

data class DateDto(
    val readable: String,
    val timestamp: String,
    val gregorian: GregorianDto,
    val hijri: HijriDto
)

data class GregorianDto(
    val date: String,      // "19-02-2026"
    val day: String,
    val year: String,
    val month: GregorianMonthDto
)

data class GregorianMonthDto(
    val number: Int,
    val en: String
)

data class HijriDto(
    val date: String,       // "01-09-1447"
    val day: String,
    val year: String,
    val month: HijriMonthDto
)

data class HijriMonthDto(
    val number: Int,
    val en: String,
    val ar: String
)

data class MetaDto(
    val latitude: Double,
    val longitude: Double,
    val timezone: String
)
