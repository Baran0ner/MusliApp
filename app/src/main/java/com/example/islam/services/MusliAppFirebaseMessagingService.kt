package com.example.islam.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.islam.MainActivity
import com.example.islam.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM mesajlarını karşılayan servis.
 *
 * Firebase Console veya sunucu tarafından gönderilen mesajlar buraya düşer.
 * Uygulama arka plandaysa sistem bildirimi gösterir.
 * Uygulama açıkken in-app gösterim yapılabilir (ileride eklenebilir).
 *
 * Topic'ler:
 *  - "general"  → tüm kullanıcılara duyurular
 *  - "ramazan"  → Ramazan özel mesajları
 */
class MusliAppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token yenilenince Firestore'a kaydet
        // FirebaseRepository'ye erişmek için basit bir WorkManager görevi tetikle
        // (Service içinde DI doğrudan çalışmaz)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: return
        val body = message.notification?.body
            ?: message.data["body"]
            ?: return

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = CHANNEL_ID
        val manager   = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Genel Bildirimler",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "MusliApp genel duyuruları ve Ramazan mesajları" }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "musliapp_general_notifications"
    }
}
