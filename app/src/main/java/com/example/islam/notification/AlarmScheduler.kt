package com.example.islam.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.islam.domain.model.Prayer
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.model.timeFor
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /** Schedules exact alarms for all prayers in the given PrayerTime */
    fun schedulePrayerAlarms(prayerTime: PrayerTime) {
        Prayer.entries.forEach { prayer ->
            val timeStr = prayerTime.timeFor(prayer)
            scheduleSingleAlarm(prayer, timeStr)
        }
    }

    private fun scheduleSingleAlarm(prayer: Prayer, timeStr: String) {
        val parts = timeStr.split(":")
        if (parts.size < 2) return

        val triggerCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            set(Calendar.MINUTE, parts[1].toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the time has already passed today, skip
        if (triggerCal.before(Calendar.getInstance())) return

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_NAME, prayer.turkishName)
            putExtra(PrayerAlarmReceiver.EXTRA_PRAYER_TIME, timeStr)
            putExtra(PrayerAlarmReceiver.EXTRA_CHANNEL_ID, prayer.notificationChannelId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayer.ordinal,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Android 12+ (API 31): check permission before scheduling exact alarm.
        // If not granted, fall back to a 60-second inexact window (setWindow).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerCal.timeInMillis,
                    pendingIntent
                )
            } else {
                // Fallback: 60-second window alarm — still fires close to prayer time
                alarmManager.setWindow(
                    AlarmManager.RTC_WAKEUP,
                    triggerCal.timeInMillis,
                    60_000L,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerCal.timeInMillis,
                pendingIntent
            )
        }
    }

    /** Cancels all prayer alarms */
    fun cancelAllAlarms() {
        Prayer.entries.forEach { prayer ->
            val intent = Intent(context, PrayerAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                prayer.ordinal,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }
}
