package com.example.islam.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.islam.worker.PrayerTimeUpdateWorker

/**
 * Telefon yeniden başlatıldığında veya uygulama güncellenmesinde tetiklenir.
 *
 * AlarmManager'ın tüm alarmları sistem yeniden başlatılınca silinir.
 * Bu receiver iki aşamalı kurtarma yapar:
 *
 * 1. **Anında tek seferlik iş** → DataStore ayarlarını okur, API'den bugünün
 *    namaz vakitlerini çeker ve AlarmScheduler ile alarmları hemen yeniden kurar.
 *
 * 2. **Günlük periyodik iş** → Gece yarısı her gün otomatik güncelleyen
 *    WorkManager periyodik görevini sıfırlar (kapanınca iptal olmuştu).
 *
 * Manifest gereksinimleri:
 *  - `RECEIVE_BOOT_COMPLETED` izni
 *  - `ACTION_BOOT_COMPLETED` + `ACTION_MY_PACKAGE_REPLACED` intent filter'ı
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return  // beklenmedik action → görmezden gel

        val workManager = WorkManager.getInstance(context)

        // ── 1. Bugünün alarmlarını hemen yeniden kur ──────────────────────────
        // Bağlantı gerektiriyor; yoksa ağ açılınca otomatik retry yapar (EXPONENTIAL).
        val immediateRequest = OneTimeWorkRequestBuilder<PrayerTimeUpdateWorker>()
            .build()

        workManager.enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,   // tekrar boot gelirse çakışma olmaz
            immediateRequest
        )

        // ── 2. Günlük periyodik güncellemeyi yeniden başlat ──────────────────
        // Worker gece yarısı çalışarak alarmları ertesi güne günceller.
        // PeriodicWork zaten KEEP politikasıyla çalışıyor;
        // boot sonrası bir kez çağırmak yeterli.
        PrayerTimeUpdateWorker.enqueueDailyWork(context)
    }

    companion object {
        private const val IMMEDIATE_WORK_NAME = "prayer_alarm_boot_reschedule"
    }
}
