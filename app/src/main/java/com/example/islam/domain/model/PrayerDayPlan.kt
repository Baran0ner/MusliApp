package com.example.islam.domain.model

data class PrayerDayPlan(
    val dateIso: String,
    val hijriDate: String,
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)
