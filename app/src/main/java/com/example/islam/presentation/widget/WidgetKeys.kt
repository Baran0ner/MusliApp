package com.example.islam.presentation.widget

import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Glance PreferencesGlanceStateDefinition ile kullanılan DataStore key'leri.
 * PrayerTimeUpdateWorker bu key'lere yazar, NextPrayerWidget okur.
 */
object WidgetKeys {
    val NEXT_PRAYER_NAME = stringPreferencesKey("widget_next_prayer_name")
    val REMAINING_TIME   = stringPreferencesKey("widget_remaining_time")
    val GREGORIAN_DATE   = stringPreferencesKey("widget_gregorian_date")
    val HIJRI_DATE       = stringPreferencesKey("widget_hijri_date")
}
