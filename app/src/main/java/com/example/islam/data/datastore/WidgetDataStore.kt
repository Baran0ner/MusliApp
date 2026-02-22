package com.example.islam.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Widget'ın ihtiyaç duyduğu minimal veriyi tutan DataStore.
 *
 * GlanceAppWidget doğrudan Room veya ViewModel'a erişemeyeceği için
 * PrayerTimeUpdateWorker, namaz vakitlerini güncelledikten sonra
 * bu DataStore'u da yazar. Glance widget buradan okur.
 *
 * Saklanan alanlar:
 *  - nextPrayerName  → "İkindi"
 *  - remainingTime   → "01:45:00" (HH:mm:ss)
 *  - gregorianDate   → "22 Şubat 2026"
 *  - hijriDate       → "23 Şaban 1447"
 */
private val Context.widgetDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "widget_data")

@Singleton
class WidgetDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    internal object Keys {
        val NEXT_PRAYER_NAME = stringPreferencesKey("widget_next_prayer_name")
        val REMAINING_TIME   = stringPreferencesKey("widget_remaining_time")
        val GREGORIAN_DATE   = stringPreferencesKey("widget_gregorian_date")
        val HIJRI_DATE       = stringPreferencesKey("widget_hijri_date")
    }

    data class WidgetData(
        val nextPrayerName: String = "—",
        val remainingTime: String  = "--:--",
        val gregorianDate: String  = "",
        val hijriDate: String      = ""
    )

    val widgetData: Flow<WidgetData> = context.widgetDataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            WidgetData(
                nextPrayerName = prefs[Keys.NEXT_PRAYER_NAME] ?: "—",
                remainingTime  = prefs[Keys.REMAINING_TIME]   ?: "--:--",
                gregorianDate  = prefs[Keys.GREGORIAN_DATE]   ?: "",
                hijriDate      = prefs[Keys.HIJRI_DATE]        ?: ""
            )
        }

    suspend fun update(
        nextPrayerName: String,
        remainingTime: String,
        gregorianDate: String,
        hijriDate: String
    ) {
        context.widgetDataStore.edit { prefs ->
            prefs[Keys.NEXT_PRAYER_NAME] = nextPrayerName
            prefs[Keys.REMAINING_TIME]   = remainingTime
            prefs[Keys.GREGORIAN_DATE]   = gregorianDate
            prefs[Keys.HIJRI_DATE]        = hijriDate
        }
    }

    /** Context-only erişim — Glance widget içinde DI olmadan çağrılır. */
    companion object {
        private val Context.store: DataStore<Preferences>
            by preferencesDataStore(name = "widget_data")

        suspend fun readWidgetData(context: Context): WidgetData {
            return try {
                val prefs = context.store.data
                    .catch { emit(emptyPreferences()) }
                    .map { it }.let {
                        var result = emptyPreferences()
                        it.collect { p -> result = p; return@collect }
                        result
                    }
                WidgetData(
                    nextPrayerName = prefs[Keys.NEXT_PRAYER_NAME] ?: "—",
                    remainingTime  = prefs[Keys.REMAINING_TIME]   ?: "--:--",
                    gregorianDate  = prefs[Keys.GREGORIAN_DATE]   ?: "",
                    hijriDate      = prefs[Keys.HIJRI_DATE]        ?: ""
                )
            } catch (e: Exception) {
                WidgetData()
            }
        }
    }
}
