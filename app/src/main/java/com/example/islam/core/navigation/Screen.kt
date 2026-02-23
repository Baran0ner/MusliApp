package com.example.islam.core.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")   // ilk açılış akışı
    object Home        : Screen("home")
    object Quran       : Screen("quran")
    object SurahReader : Screen("surah_reader/{surahId}/{surahName}/{isJuz}/{startVerse}") {
        fun route(surahId: Int, surahName: String, isJuz: Boolean = false, startVerse: Int = 0) =
            "surah_reader/$surahId/$surahName/$isJuz/$startVerse"
    }
    object PrayerTimes : Screen("prayer_times")
    object RamadanPlanner : Screen("ramadan_planner")
    object Dhikr       : Screen("dhikr")
    object Qibla       : Screen("qibla")
    object Settings    : Screen("settings")
    object GoogleAuth  : Screen("google_auth")
}
