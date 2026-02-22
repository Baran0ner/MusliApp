package com.example.islam.services

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.islam.MainActivity
import com.example.islam.R
import com.example.islam.domain.model.Prayer

/**
 * Ezan vaktinde çalışan Foreground Service.
 *
 * - [ACTION_START] → Servisi başlatır, foreground bildirimi gösterir ve ezan sesini çalar.
 * - [ACTION_STOP]  → MediaPlayer'ı durdurur, bildirimi kaldırır ve servisi kapatır.
 *
 * Bildirime tıklamak veya "Durdur" butonuna basmak her ikisi de [ACTION_STOP] gönderir.
 *
 * Android 14+ (API 34) için [ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK]
 * hem manifest'te hem de [startForeground] çağrısında belirtilmiştir.
 */
class EzanForegroundService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return START_NOT_STICKY
                val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
                val channelId  = intent.getStringExtra(EXTRA_CHANNEL_ID)  ?: Prayer.NOTIFICATION_CHANNEL_ID
                startEzan(prayerName, prayerTime, channelId)
            }
            ACTION_STOP -> stopEzan()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ezan başlat / durdur
    // ─────────────────────────────────────────────────────────────────────────

    private fun startEzan(prayerName: String, prayerTime: String, channelId: String) {
        val notification = buildNotification(prayerName, prayerTime, channelId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // API 34+: foreground service type koda da verilmeli
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        playEzan()
    }

    private fun playEzan() {
        releasePlayer()
        try {
            val afd = resources.openRawResourceFd(R.raw.ezan_sesi)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                prepare()
                // Ses bitince servisi otomatik kapat
                setOnCompletionListener { stopEzan() }
                start()
            }
        } catch (_: Exception) {
            // Ses dosyası yoksa veya çalınamazsa servisi kapat
            stopEzan()
        }
    }

    private fun stopEzan() {
        releasePlayer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun releasePlayer() {
        mediaPlayer?.runCatching { stop() }
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bildirim
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Bildirime tıklamak ve "Durdur" butonu: her ikisi de [ACTION_STOP] gönderir.
     * Bu sayede kullanıcı hangi yolla etkileşirse etkileşsin ses durur.
     */
    private fun buildNotification(
        prayerName: String,
        prayerTime: String,
        channelId: String
    ): android.app.Notification {

        val stopPendingIntent = PendingIntent.getService(
            this,
            REQUEST_STOP,
            Intent(this, EzanForegroundService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Bildirime tıklanınca uygulamayı aç ve ezan dursun
        val openAndStopIntent = PendingIntent.getActivities(
            this,
            REQUEST_OPEN,
            arrayOf(
                Intent(this, EzanForegroundService::class.java).apply { action = ACTION_STOP },
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            ),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$prayerName Vakti \uD83D\uDD54")          // 🔔
            .setContentText("$prayerName namazının vakti geldi: $prayerTime")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(openAndStopIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Durdur",
                stopPendingIntent
            )
            .build()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Companion
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        const val ACTION_START      = "com.example.islam.action.EZAN_START"
        const val ACTION_STOP       = "com.example.islam.action.EZAN_STOP"

        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_CHANNEL_ID  = "extra_channel_id"

        private const val NOTIFICATION_ID = 9001
        private const val REQUEST_STOP    = 1
        private const val REQUEST_OPEN    = 2

        /** [PrayerAlarmReceiver]'dan çağrılmak üzere hazır Intent factory. */
        fun buildStartIntent(
            context: Context,
            prayerName: String,
            prayerTime: String,
            channelId: String
        ): Intent = Intent(context, EzanForegroundService::class.java).apply {
            action = ACTION_START
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_PRAYER_TIME, prayerTime)
            putExtra(EXTRA_CHANNEL_ID,  channelId)
        }
    }
}
