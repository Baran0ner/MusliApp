package com.example.islam.domain.model

data class IbadahDayStatus(
    val dateIso: String,
    val fastDone: Boolean,
    val fajrDone: Boolean,
    val dhuhrDone: Boolean,
    val asrDone: Boolean,
    val maghribDone: Boolean,
    val ishaDone: Boolean,
    val prayedCount: Int,
    val dhikrCount: Int,
    val quranMinutes: Int
)
