package com.example.islam.domain.model

enum class PrayerType(val displayName: String, val arabicName: String) {
    FAJR    ("Sabah",  "الفجر"),
    DHUHR   ("Öğle",   "الظهر"),
    ASR     ("İkindi", "العصر"),
    MAGHRIB ("Akşam",  "المغرب"),
    ISHA    ("Yatsı",  "العشاء")
}
