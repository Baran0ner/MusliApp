package com.example.islam.domain.model

data class PrayerTime(
    val id: Long = 0,
    val date: String,        // "19-02-2026"
    val imsak: String,       // "05:32"
    val fajr: String,        // "05:42"
    val sunrise: String,     // "07:14"
    val dhuhr: String,       // "12:29"
    val asr: String,         // "15:33"
    val maghrib: String,     // "18:44"
    val isha: String,        // "20:09"
    val hijriDate: String,   // "01 رمضان 1447"
    val city: String,
    val country: String
)
