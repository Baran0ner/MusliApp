package com.example.islam.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.islam.domain.model.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val CITY                = stringPreferencesKey("city")
        val COUNTRY             = stringPreferencesKey("country")
        val LATITUDE            = doublePreferencesKey("latitude")
        val LONGITUDE           = doublePreferencesKey("longitude")
        val CALC_METHOD         = intPreferencesKey("calculation_method")
        val NOTIFICATIONS       = booleanPreferencesKey("notifications_enabled")
        val USE_GPS             = booleanPreferencesKey("use_gps")
        // Yeni: 0=Sistem, 1=Koyu, 2=Açık (eski "dark_theme" bool key'in yerini alıyor)
        val APP_THEME           = intPreferencesKey("app_theme")
        val SCHOOL              = intPreferencesKey("school")       // 0=Şafii, 1=Hanefi
        val LANGUAGE            = stringPreferencesKey("language")  // "tr" | "en" | "ar"
        val ONBOARDING_DONE     = booleanPreferencesKey("onboarding_done")
        // Namaz takibi
        val PRAYER_STREAK       = intPreferencesKey("prayer_streak")
        val STREAK_LAST_DATE    = stringPreferencesKey("streak_last_date")
        val COMPLETED_PRAYERS   = stringPreferencesKey("completed_prayers_today") // "date|prayer1,prayer2"
        // Hicri takvim sapması (-2..+2)
        val HIJRI_OFFSET        = intPreferencesKey("hijri_offset")
        // Namaz bildirim türleri: "fajr:0,dhuhr:0,asr:0,maghrib:0,isha:0"
        val PRAYER_NOTIF_TYPES  = stringPreferencesKey("prayer_notif_types")
        // Son okunan sure (Kuran Last Read)
        val LAST_READ_SURAH_ID   = intPreferencesKey("last_read_surah_id")
        val LAST_READ_SURAH_NAME = stringPreferencesKey("last_read_surah_name")
        val LAST_READ_VERSE      = intPreferencesKey("last_read_verse")
        val LAST_READ_TOTAL      = intPreferencesKey("last_read_total")
        /** Bilinen sekmesi — yer imli sure numaraları, virgülle ayrılmış "1,2,5" */
        val BOOKMARKED_SURAH_IDS = stringPreferencesKey("bookmarked_surah_ids")
    }

    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            UserPreferences(
                city                 = prefs[Keys.CITY] ?: "Istanbul",
                country              = prefs[Keys.COUNTRY] ?: "Turkey",
                latitude             = prefs[Keys.LATITUDE] ?: 41.0082,
                longitude            = prefs[Keys.LONGITUDE] ?: 28.9784,
                calculationMethod    = prefs[Keys.CALC_METHOD] ?: 13,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
                useGps               = prefs[Keys.USE_GPS] ?: false,
                appTheme             = prefs[Keys.APP_THEME] ?: 0,
                school               = prefs[Keys.SCHOOL] ?: 0,
                language             = prefs[Keys.LANGUAGE] ?: "tr",
                hijriOffset          = prefs[Keys.HIJRI_OFFSET] ?: 0,
                prayerNotifTypes     = prefs[Keys.PRAYER_NOTIF_TYPES]
                    ?: "fajr:0,dhuhr:0,asr:0,maghrib:0,isha:0"
            )
        }

    suspend fun updateCity(city: String, country: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CITY] = city
            prefs[Keys.COUNTRY] = country
        }
    }

    suspend fun updateCoordinates(lat: Double, lon: Double) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LATITUDE] = lat
            prefs[Keys.LONGITUDE] = lon
        }
    }

    suspend fun updateNotifications(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.NOTIFICATIONS] = enabled }
    }

    suspend fun updateUseGps(useGps: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.USE_GPS] = useGps }
    }

    suspend fun updateAppTheme(theme: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.APP_THEME] = theme }
    }

    // Geriye dönük uyumluluk: eski darkTheme boolean'ı int'e çevirip kaydeder
    suspend fun updateDarkTheme(dark: Boolean) {
        updateAppTheme(if (dark) 1 else 0)
    }

    suspend fun updateCalculationMethod(method: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.CALC_METHOD] = method }
    }

    suspend fun updateSchool(school: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.SCHOOL] = school }
    }

    suspend fun updateLanguage(language: String) {
        context.dataStore.edit { prefs -> prefs[Keys.LANGUAGE] = language }
    }

    suspend fun updateHijriOffset(offset: Int) {
        val clamped = offset.coerceIn(-2, 2)
        context.dataStore.edit { prefs -> prefs[Keys.HIJRI_OFFSET] = clamped }
    }

    /** Tek bir namaz vakti için bildirim türünü günceller.
     *  @param prayerId "fajr" | "dhuhr" | "asr" | "maghrib" | "isha"
     *  @param type     0=Sessiz, 1=Kısa Uyarı, 2=Tam Ezan
     */
    suspend fun updatePrayerNotifType(prayerId: String, type: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.PRAYER_NOTIF_TYPES]
                ?: "fajr:0,dhuhr:0,asr:0,maghrib:0,isha:0"
            val map = current.split(",")
                .associate {
                    val parts = it.split(":")
                    parts[0] to parts.getOrElse(1) { "0" }
                }
                .toMutableMap()
            map[prayerId] = type.toString()
            prefs[Keys.PRAYER_NOTIF_TYPES] = map.entries.joinToString(",") { "${it.key}:${it.value}" }
        }
    }

    // ── Onboarding ────────────────────────────────────────────────────────────

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[Keys.ONBOARDING_DONE] ?: false }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { prefs -> prefs[Keys.ONBOARDING_DONE] = true }
    }

    suspend fun resetOnboarding() {
        context.dataStore.edit { prefs -> prefs[Keys.ONBOARDING_DONE] = false }
    }

    // ── Kuran Last Read ──────────────────────────────────────────────────────

    data class LastRead(
        val surahId: Int,
        val surahName: String,
        val verseNumber: Int,
        val totalVerses: Int
    )

    val lastRead: Flow<LastRead?> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            val id = prefs[Keys.LAST_READ_SURAH_ID] ?: return@map null
            val name = prefs[Keys.LAST_READ_SURAH_NAME] ?: return@map null
            val verse = prefs[Keys.LAST_READ_VERSE] ?: 1
            val total = prefs[Keys.LAST_READ_TOTAL] ?: 1
            LastRead(surahId = id, surahName = name, verseNumber = verse, totalVerses = total)
        }

    suspend fun updateLastRead(surahId: Int, surahName: String, verseNumber: Int, totalVerses: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_READ_SURAH_ID] = surahId
            prefs[Keys.LAST_READ_SURAH_NAME] = surahName
            prefs[Keys.LAST_READ_VERSE] = verseNumber.coerceIn(1, totalVerses)
            prefs[Keys.LAST_READ_TOTAL] = totalVerses.coerceAtLeast(1)
        }
    }

    // ── Bilinen (yer imli sureler) ───────────────────────────────────────────

    val bookmarkedSurahIds: Flow<Set<Int>> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            val raw = prefs[Keys.BOOKMARKED_SURAH_IDS] ?: ""
            if (raw.isBlank()) emptySet()
            else raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
        }

    suspend fun toggleBookmarkSurah(surahNumber: Int) {
        context.dataStore.edit { prefs ->
            val raw = prefs[Keys.BOOKMARKED_SURAH_IDS] ?: ""
            val current = if (raw.isBlank()) emptySet()
            else raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            val next = if (surahNumber in current) current - surahNumber else current + surahNumber
            prefs[Keys.BOOKMARKED_SURAH_IDS] = next.sorted().joinToString(",")
        }
    }

    // ── Namaz Takibi ─────────────────────────────────────────────────────────

    /** Bugün tamamlanan namaz id'leri (virgülle ayrılmış), format: "YYYY-MM-DD|id1,id2" */
    val completedPrayersToday: Flow<Set<String>> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            val today = java.time.LocalDate.now().toString()
            val raw = prefs[Keys.COMPLETED_PRAYERS] ?: ""
            if (raw.startsWith(today)) {
                raw.substringAfter("|").split(",").filter { it.isNotBlank() }.toSet()
            } else emptySet()
        }

    val prayerStreak: Flow<Int> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> prefs[Keys.PRAYER_STREAK] ?: 0 }

    suspend fun togglePrayerCompleted(prayerId: String, allPrayerIds: List<String>) {
        val today = java.time.LocalDate.now().toString()
        context.dataStore.edit { prefs ->
            val raw = prefs[Keys.COMPLETED_PRAYERS] ?: ""
            val currentSet = if (raw.startsWith(today)) {
                raw.substringAfter("|").split(",").filter { it.isNotBlank() }.toMutableSet()
            } else mutableSetOf()

            if (prayerId in currentSet) currentSet.remove(prayerId)
            else currentSet.add(prayerId)

            prefs[Keys.COMPLETED_PRAYERS] = "$today|${currentSet.joinToString(",")}"

            // Eğer tüm namazlar tamamlandıysa streak artır
            if (currentSet.containsAll(allPrayerIds)) {
                val lastDate = prefs[Keys.STREAK_LAST_DATE] ?: ""
                val yesterday = java.time.LocalDate.now().minusDays(1).toString()
                val currentStreak = prefs[Keys.PRAYER_STREAK] ?: 0
                if (lastDate != today) {
                    prefs[Keys.PRAYER_STREAK] = if (lastDate == yesterday) currentStreak + 1 else 1
                    prefs[Keys.STREAK_LAST_DATE] = today
                }
            }
        }
    }
}
