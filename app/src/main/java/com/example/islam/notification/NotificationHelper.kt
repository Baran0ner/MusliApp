package com.example.islam.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.islam.MainActivity
import com.example.islam.R
import com.example.islam.domain.model.Prayer

object NotificationHelper {
    const val GENERAL_ANNOUNCEMENTS_CHANNEL_ID = "channel_general_announcements"
    private const val GENERAL_ANNOUNCEMENTS_CHANNEL_NAME = "Genel Duyurular"

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
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                runCatching { setBypassDnd(true) }
            }
            manager.createNotificationChannel(channel)
        }
        createGeneralAnnouncementsChannel(manager)
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
        channelId: String = Prayer.NOTIFICATION_CHANNEL_ID,
        displayName: String? = null,
        personalizedNotificationsEnabled: Boolean = true
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val fullScreenIntent = PendingIntent.getActivity(
            context,
            prayerName.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_kaaba)
            .setContentTitle(
                NotificationTextFormatter.prayerTitle(
                    prayerName = prayerName,
                    displayName = displayName,
                    personalizedNotificationsEnabled = personalizedNotificationsEnabled
                )
            )
            .setContentText(
                NotificationTextFormatter.prayerBody(
                    prayerName = prayerName,
                    prayerTime = prayerTime,
                    displayName = displayName,
                    personalizedNotificationsEnabled = personalizedNotificationsEnabled
                )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)   // kilit ekranında göster
            .setFullScreenIntent(fullScreenIntent, true)
            .setAutoCancel(true)
            .build()

        manager.notify(prayerName.hashCode(), notification)
    }

    /**
     * Shows standard-priority FCM announcement notifications (admin broadcasts).
     */
    fun showGeneralAnnouncementNotification(
        context: Context,
        title: String,
        body: String,
        notificationId: Int = System.currentTimeMillis().toInt(),
        displayName: String? = null,
        personalizedNotificationsEnabled: Boolean = true
    ) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createGeneralAnnouncementsChannel(manager)

        val launchIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedBody = NotificationTextFormatter.announcementBody(
            body = body,
            displayName = displayName,
            personalizedNotificationsEnabled = personalizedNotificationsEnabled
        )

        val notification = NotificationCompat.Builder(context, GENERAL_ANNOUNCEMENTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_kaaba)
            .setContentTitle(title)
            .setContentText(formattedBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(formattedBody))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(launchIntent)
            .build()

        manager.notify(notificationId, notification)
    }

    private fun createGeneralAnnouncementsChannel(manager: NotificationManager) {
        val channel = NotificationChannel(
            GENERAL_ANNOUNCEMENTS_CHANNEL_ID,
            GENERAL_ANNOUNCEMENTS_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Yonetici duyurulari, kutlama mesajlari ve uygulama guncellemeleri"
            enableVibration(true)
            setShowBadge(true)
        }
        manager.createNotificationChannel(channel)
    }
}
