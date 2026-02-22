package com.example.islam.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.islam.R
import com.example.islam.domain.model.Prayer

object NotificationHelper {

    /**
     * Creates one notification channel per prayer (7 total) so users can
     * enable/disable each prayer's notification individually in system settings.
     */
    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        Prayer.entries.forEach { prayer ->
            val channel = NotificationChannel(
                prayer.notificationChannelId,
                prayer.turkishName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "${prayer.turkishName} vakti bildirimi"
                enableVibration(true)
                setShowBadge(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a high-priority notification on the prayer's dedicated channel.
     *
     * @param channelId Per-prayer channel ID ([Prayer.notificationChannelId]).
     *                  Falls back to the shared channel if omitted.
     */
    fun showPrayerNotification(
        context: Context,
        prayerName: String,
        prayerTime: String,
        channelId: String = Prayer.NOTIFICATION_CHANNEL_ID
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$prayerName Vakti")
            .setContentText("$prayerName namazının vakti geldi: $prayerTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)   // kilit ekranında göster
            .setAutoCancel(true)
            .build()

        manager.notify(prayerName.hashCode(), notification)
    }
}
