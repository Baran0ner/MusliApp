package com.example.islam.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtil {

    private val turkishLocale = Locale("tr", "TR")

    /** Returns today's date as "dd-MM-yyyy" (Aladhan API format) */
    fun todayFormatted(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        return sdf.format(Date())
    }

    /** Returns today as "yyyy-MM-dd" for Room queries */
    fun todayIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    /** Parses "HH:mm" string to Calendar for today */
    fun todayCalendarAt(timeStr: String): Calendar {
        val parts = timeStr.substringBefore(" ").split(":")
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            set(Calendar.MINUTE, parts[1].toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    /** Formats milliseconds remaining into "HH:mm:ss" countdown string */
    fun formatCountdown(millis: Long): String {
        if (millis <= 0L) return "00:00:00"
        val hours = (millis / 3_600_000) % 24
        val minutes = (millis / 60_000) % 60
        val seconds = (millis / 1_000) % 60
        return "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    /** Formats a Date to Turkish long format: "19 Şubat 2026, Perşembe" */
    fun formatDateLong(date: Date = Date()): String {
        val sdf = SimpleDateFormat("d MMMM yyyy, EEEE", turkishLocale)
        return sdf.format(date)
    }

    /** Strips timezone suffix from Aladhan time strings like "04:32 (EET)" */
    fun String.cleanTime(): String = this.substringBefore(" ")
}
