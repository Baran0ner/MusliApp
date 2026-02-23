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
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val MULTI_SPACE_REGEX = Regex("\\s+")
        private val DISPLAY_NAME_REGEX = Regex("^[\\p{L}' ]+$")
    }

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
        val DISPLAY_NAME        = stringPreferencesKey("display_name")
        val PERSONALIZED_ADDRESSING = booleanPreferencesKey("personalized_addressing")
        val PERSONALIZED_NOTIFICATIONS = booleanPreferencesKey("personalized_notifications")
        val NAME_PROMPT_DISMISSED = booleanPreferencesKey("name_prompt_dismissed")
        val ONBOARDING_DONE     = booleanPreferencesKey("onboarding_done")
        // Namaz takibi
        val PRAYER_STREAK       = intPreferencesKey("prayer_streak")
        val STREAK_LAST_DATE    = stringPreferencesKey("streak_last_date")
        val COMPLETED_PRAYERS   = stringPreferencesKey("completed_prayers_today") // "date|prayer1,prayer2"
        // Home streak kartı (haftalık gün dolumu)
        val HOME_STREAK_WEEK_START = stringPreferencesKey("home_streak_week_start") // ISO date, hafta başlangıcı (Pazartesi)
        val HOME_STREAK_WEEK_MASK  = intPreferencesKey("home_streak_week_mask")     // 7-bit maske: Mon..Sun
        val HOME_STREAK_LAST_MARK  = stringPreferencesKey("home_streak_last_mark")
        // Hicri takvim sapması (-2..+2)
        val HIJRI_OFFSET        = intPreferencesKey("hijri_offset")
        // Günlük namaz hedefi (1..5)
        val DAILY_PRAYER_GOAL   = intPreferencesKey("daily_prayer_goal")
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
                dailyPrayerGoal      = (prefs[Keys.DAILY_PRAYER_GOAL] ?: 5).coerceIn(1, 5),
                prayerNotifTypes     = prefs[Keys.PRAYER_NOTIF_TYPES]
                    ?: "fajr:0,dhuhr:0,asr:0,maghrib:0,isha:0",
                displayName          = prefs[Keys.DISPLAY_NAME] ?: "",
                personalizedAddressingEnabled = prefs[Keys.PERSONALIZED_ADDRESSING] ?: true,
                personalizedNotificationsEnabled = prefs[Keys.PERSONALIZED_NOTIFICATIONS] ?: true,
                namePromptDismissed  = prefs[Keys.NAME_PROMPT_DISMISSED] ?: false
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

    suspend fun updateDisplayName(name: String) {
        val sanitized = sanitizeDisplayName(name) ?: return
        context.dataStore.edit { prefs -> prefs[Keys.DISPLAY_NAME] = sanitized }
    }

    suspend fun clearDisplayName() {
        context.dataStore.edit { prefs -> prefs[Keys.DISPLAY_NAME] = "" }
    }

    suspend fun setPersonalizedAddressingEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.PERSONALIZED_ADDRESSING] = enabled }
    }

    suspend fun setPersonalizedNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.PERSONALIZED_NOTIFICATIONS] = enabled }
    }

    suspend fun setNamePromptDismissed(dismissed: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.NAME_PROMPT_DISMISSED] = dismissed }
    }

    suspend fun seedDisplayNameIfEmpty(candidate: String?) {
        val sanitized = sanitizeDisplayName(candidate ?: return) ?: return
        context.dataStore.edit { prefs ->
            if ((prefs[Keys.DISPLAY_NAME] ?: "").isBlank()) {
                prefs[Keys.DISPLAY_NAME] = sanitized
            }
        }
    }

    suspend fun updateHijriOffset(offset: Int) {
        val clamped = offset.coerceIn(-2, 2)
        context.dataStore.edit { prefs -> prefs[Keys.HIJRI_OFFSET] = clamped }
    }

    // ── Günlük Namaz Hedefi ────────────────────────────────────────────────

    suspend fun updateDailyPrayerGoal(goal: Int) {
        val clamped = goal.coerceIn(1, 5)
        val today = java.time.LocalDate.now().toString()
        val yesterday = java.time.LocalDate.now().minusDays(1).toString()
        context.dataStore.edit { prefs ->
            prefs[Keys.DAILY_PRAYER_GOAL] = clamped

            // Hedef düşürüldüyse ve bugün zaten tamamlandıysa, streak'i güncelle
            val raw = prefs[Keys.COMPLETED_PRAYERS] ?: ""
            val completedCount = if (raw.startsWith(today)) {
                raw.substringAfter("|").split(",").filter { it.isNotBlank() }.size
            } else 0

            if (completedCount >= clamped) {
                val lastDate = prefs[Keys.STREAK_LAST_DATE] ?: ""
                if (lastDate != today) {
                    val currentStreak = prefs[Keys.PRAYER_STREAK] ?: 0
                    prefs[Keys.PRAYER_STREAK] = if (lastDate == yesterday) currentStreak + 1 else 1
                    prefs[Keys.STREAK_LAST_DATE] = today
                }
            }
        }
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

    suspend fun setOnboardingCompleted(suppressNamePrompt: Boolean = false) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_DONE] = true
            if (suppressNamePrompt) prefs[Keys.NAME_PROMPT_DISMISSED] = true
        }
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

    /** Home streak kartı haftalık maskesi (Mon..Sun => bit 0..6). */
    val homeStreakWeekMask: Flow<Int> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            val today = LocalDate.now()
            val currentWeekStart = homeWeekStart(today).toString()
            val storedWeekStart = prefs[Keys.HOME_STREAK_WEEK_START] ?: ""
            if (storedWeekStart == currentWeekStart) (prefs[Keys.HOME_STREAK_WEEK_MASK] ?: 0) else 0
        }

    suspend fun togglePrayerCompleted(prayerId: String) {
        val today = java.time.LocalDate.now().toString()
        context.dataStore.edit { prefs ->
            val raw = prefs[Keys.COMPLETED_PRAYERS] ?: ""
            val currentSet = if (raw.startsWith(today)) {
                raw.substringAfter("|").split(",").filter { it.isNotBlank() }.toMutableSet()
            } else mutableSetOf()

            if (prayerId in currentSet) currentSet.remove(prayerId)
            else currentSet.add(prayerId)

            prefs[Keys.COMPLETED_PRAYERS] = "$today|${currentSet.joinToString(",")}"

            // Hedef tamamlandıysa streak artır
            val goal = (prefs[Keys.DAILY_PRAYER_GOAL] ?: 5).coerceIn(1, 5)
            if (currentSet.size >= goal) {
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

    /**
     * Gün değiştiyse ve son hedef tarihi dün değilse streak'i sıfırlar.
     * (Duolingo mantığı: yeni gün başında streak korunur; dün de yoksa kırılır.)
     */
    suspend fun ensureStreakUpToDate() {
        val today = java.time.LocalDate.now().toString()
        val yesterday = java.time.LocalDate.now().minusDays(1).toString()
        context.dataStore.edit { prefs ->
            val lastDate = prefs[Keys.STREAK_LAST_DATE] ?: ""
            if (lastDate.isNotBlank() && lastDate != today && lastDate != yesterday) {
                prefs[Keys.PRAYER_STREAK] = 0
            }
        }
    }

    /**
     * Home ekranına her girişte bugünün gününü haftalık streak satırında işaretler.
     * Aynı gün içinde tekrar girişte tekrar yazmaz.
     */
    suspend fun markHomeEntryForToday() {
        val today = LocalDate.now()
        val todayIso = today.toString()
        val currentWeekStart = homeWeekStart(today).toString()
        val dayIndex = homeWeekDayIndex(today.dayOfWeek)
        context.dataStore.edit { prefs ->
            val alreadyMarkedToday = (prefs[Keys.HOME_STREAK_LAST_MARK] ?: "") == todayIso
            val storedWeekStart = prefs[Keys.HOME_STREAK_WEEK_START] ?: ""
            var mask = if (storedWeekStart == currentWeekStart) {
                prefs[Keys.HOME_STREAK_WEEK_MASK] ?: 0
            } else 0

            if (!alreadyMarkedToday) {
                mask = mask or (1 shl dayIndex)
                prefs[Keys.HOME_STREAK_LAST_MARK] = todayIso
            }

            prefs[Keys.HOME_STREAK_WEEK_START] = currentWeekStart
            prefs[Keys.HOME_STREAK_WEEK_MASK] = mask
        }
    }

    private fun homeWeekStart(date: LocalDate): LocalDate {
        val backDays = (date.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong() // Mon=1 ... Sun=7
        return date.minusDays(backDays)
    }

    private fun homeWeekDayIndex(dayOfWeek: DayOfWeek): Int = dayOfWeek.value - DayOfWeek.MONDAY.value

    private fun sanitizeDisplayName(raw: String): String? {
        val normalized = raw
            .replace('’', '\'')
            .trim()
            .replace(MULTI_SPACE_REGEX, " ")

        if (normalized.length !in 2..24) return null
        if (!DISPLAY_NAME_REGEX.matches(normalized)) return null
        return normalized
    }
}
