package com.example.islam.domain.model

enum class Prayer(
    val turkishName: String,
    val arabicName: String,
    val notificationChannelId: String
) {
    IMSAK("İmsak", "الإمساك", "channel_imsak"),
    FAJR("Sabah", "الفجر", "channel_fajr"),
    SUNRISE("Güneş", "الشروق", "channel_sunrise"),
    DHUHR("Öğle", "الظهر", "channel_dhuhr"),
    ASR("İkindi", "العصر", "channel_asr"),
    MAGHRIB("Akşam", "المغرب", "channel_maghrib"),
    ISHA("Yatsı", "العشاء", "channel_isha");

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "channel_prayer_times"
        const val NOTIFICATION_CHANNEL_NAME = "Namaz Vakitleri"
    }
}

/** Returns the prayer time string for a given Prayer enum from a PrayerTime object */
fun PrayerTime.timeFor(prayer: Prayer): String = when (prayer) {
    Prayer.IMSAK -> imsak
    Prayer.FAJR -> fajr
    Prayer.SUNRISE -> sunrise
    Prayer.DHUHR -> dhuhr
    Prayer.ASR -> asr
    Prayer.MAGHRIB -> maghrib
    Prayer.ISHA -> isha
}
