package com.example.islam.worker

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.islam.core.util.Resource
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.repository.PrayerTimeRepository
import com.example.islam.notification.AlarmScheduler
import com.example.islam.presentation.widget.NextPrayerWidget
import com.example.islam.presentation.widget.WidgetKeys
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

@HiltWorker
class PrayerTimeUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val prefsDataStore: UserPreferencesDataStore,
    private val alarmScheduler: AlarmScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = prefsDataStore.userPreferences.first()
            val result = prayerTimeRepository.getPrayerTimes(
                city    = prefs.city,
                country = prefs.country,
                method  = prefs.calculationMethod,
                school  = prefs.school
            )
            when (result) {
                is Resource.Success -> {
                    if (prefs.notificationsEnabled) {
                        alarmScheduler.schedulePrayerAlarms(result.data)
                    }
                    // Widget verisini güncelle
                    updateWidgetState(applicationContext, result.data)
                    Result.success()
                }
                is Resource.Error   -> Result.retry()
                is Resource.Loading -> Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Widget state güncelleme
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun updateWidgetState(context: Context, prayerTime: PrayerTime) {
        try {
            val (nextName, remaining) = resolveNextPrayer(prayerTime)
            val gregorian = formatGregorianDate()
            val hijri     = prayerTime.hijriDate

            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(NextPrayerWidget::class.java)

            glanceIds.forEach { glanceId ->
                updateAppWidgetState(
                    context            = context,
                    definition         = PreferencesGlanceStateDefinition,
                    glanceId           = glanceId
                ) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[WidgetKeys.NEXT_PRAYER_NAME] = nextName
                        this[WidgetKeys.REMAINING_TIME]   = remaining
                        this[WidgetKeys.GREGORIAN_DATE]   = gregorian
                        this[WidgetKeys.HIJRI_DATE]        = hijri
                    }
                }
            }
            NextPrayerWidget().updateAll(context)
        } catch (_: Exception) {
            // Widget kurulu değilse veya Glance başlatılmadıysa sessizce geç
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Yardımcı: sıradaki namazı ve kalan süreyi hesapla
    // ─────────────────────────────────────────────────────────────────────────

    private fun resolveNextPrayer(pt: PrayerTime): Pair<String, String> {
        val now = LocalTime.now()
        val fmt = DateTimeFormatter.ofPattern("HH:mm")

        data class PrayerEntry(val name: String, val time: LocalTime)

        val prayers = listOf(
            PrayerEntry("İmsak",   LocalTime.parse(pt.imsak,   fmt)),
            PrayerEntry("Sabah",   LocalTime.parse(pt.fajr,    fmt)),
            PrayerEntry("Güneş",   LocalTime.parse(pt.sunrise, fmt)),
            PrayerEntry("Öğle",    LocalTime.parse(pt.dhuhr,   fmt)),
            PrayerEntry("İkindi",  LocalTime.parse(pt.asr,     fmt)),
            PrayerEntry("Akşam",   LocalTime.parse(pt.maghrib, fmt)),
            PrayerEntry("Yatsı",   LocalTime.parse(pt.isha,    fmt))
        )

        val next = prayers.firstOrNull { it.time.isAfter(now) }
            ?: prayers.first() // Gece yarısı sonrası → yarının ilk vakti

        val diffSeconds = if (next.time.isAfter(now)) {
            now.until(next.time, java.time.temporal.ChronoUnit.SECONDS)
        } else {
            // Ertesi güne taşan durum
            val secondsUntilMidnight = now.until(LocalTime.MIDNIGHT, java.time.temporal.ChronoUnit.SECONDS)
            val secondsFromMidnight  = LocalTime.MIDNIGHT.until(next.time, java.time.temporal.ChronoUnit.SECONDS)
            secondsUntilMidnight + secondsFromMidnight
        }

        val hours   = diffSeconds / 3600
        val minutes = (diffSeconds % 3600) / 60
        val remaining = "%02d Sa %02d Dk".format(hours, minutes)

        return next.name to remaining
    }

    private fun formatGregorianDate(): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
        return today.format(formatter)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Companion
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        const val WORK_NAME = "prayer_time_daily_update"

        fun enqueueDailyWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<PrayerTimeUpdateWorker>(
                repeatInterval         = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setInitialDelay(calculateInitialDelayTo005(), TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        private fun calculateInitialDelayTo005(): Long {
            val now = ZonedDateTime.now()
            var nextRun = now
                .withHour(0)
                .withMinute(5)
                .withSecond(0)
                .withNano(0)

            if (!nextRun.isAfter(now)) {
                nextRun = nextRun.plusDays(1)
            }

            return Duration.between(now, nextRun).toMillis().coerceAtLeast(0L)
        }
    }
}
