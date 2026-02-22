package com.example.islam.domain.model

data class PrayerHistory(
    val date           : String,
    val isFajrPrayed   : Boolean = false,
    val isDhuhrPrayed  : Boolean = false,
    val isAsrPrayed    : Boolean = false,
    val isMaghribPrayed: Boolean = false,
    val isIshaPrayed   : Boolean = false
) {
    val prayedCount: Int
        get() = listOf(isFajrPrayed, isDhuhrPrayed, isAsrPrayed, isMaghribPrayed, isIshaPrayed).count { it }

    fun isTracked(prayerType: PrayerType) = when (prayerType) {
        PrayerType.FAJR    -> isFajrPrayed
        PrayerType.DHUHR   -> isDhuhrPrayed
        PrayerType.ASR     -> isAsrPrayed
        PrayerType.MAGHRIB -> isMaghribPrayed
        PrayerType.ISHA    -> isIshaPrayed
    }
}
