package com.example.islam.domain.model

data class WeekDay(
    val date     : String,  // "yyyy-MM-dd"
    val shortName: String,  // "Pzt", "Sal" ...
    val history  : PrayerHistory
)
