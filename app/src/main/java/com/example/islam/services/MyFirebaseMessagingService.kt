package com.example.islam.services

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.notification.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Admin tarafindan gonderilen FCM push mesajlarini yakalar ve kullaniciya bildirir.
 * Data payload ve Notification payload senaryolarini birlikte destekler.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Gelecekte backend'e kaydetmek icin gerekli.
        Log.i(TAG, "FCM token refreshed: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"]
            ?.takeIf { it.isNotBlank() }
            ?: message.notification?.title
                ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_TITLE

        val body = message.data["body"]
            ?.takeIf { it.isNotBlank() }
            ?: message.notification?.body
                ?.takeIf { it.isNotBlank() }
            ?: return

        if (!hasNotificationPermission()) {
            Log.w(TAG, "FCM message dropped because POST_NOTIFICATIONS is not granted")
            return
        }

        val personalization = runCatching {
            runBlocking { UserPreferencesDataStore(applicationContext).userPreferences.first() }
        }.getOrNull()

        NotificationHelper.showGeneralAnnouncementNotification(
            context = applicationContext,
            title = title,
            body = body,
            displayName = personalization?.displayName,
            personalizedNotificationsEnabled = personalization?.personalizedNotificationsEnabled ?: true
        )
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private const val DEFAULT_TITLE = "MusliApp"
    }
}
