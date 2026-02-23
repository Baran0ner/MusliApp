package com.example.islam.services

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.islam.notification.NotificationHelper
import com.google.firebase.messaging.RemoteMessage
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MyFirebaseMessagingServiceE2ETest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val notificationManager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @Before
    fun setUp() {
        NotificationHelper.createNotificationChannels(context)
        notificationManager.cancelAll()

        ensurePostNotificationsPermissionIfNeeded()
        // Android 13+ cihazlarda testin anlamlı olabilmesi için izin verilmiş olmalı.
        assumeTrue(hasPostNotificationsPermission())
    }

    @After
    fun tearDown() {
        notificationManager.cancelAll()
    }

    @Test
    fun dataPayload_postsGeneralAnnouncementNotification() {
        val title = "QA Data Payload"
        val body = "FCM data payload test"
        val service = createTestService()
        val message = RemoteMessage.Builder("qa-data@test")
            .setMessageId("msg-data-1")
            .addData("title", title)
            .addData("body", body)
            .build()

        service.onMessageReceived(message)
        assertTrue(
            "Notification not posted for data payload",
            waitForNotification(title = title, body = body)
        )
    }

    @Test
    fun notificationPayload_postsGeneralAnnouncementNotification() {
        val title = "QA Notification Payload"
        val body = "FCM notification payload test"
        val service = createTestService()
        val message = RemoteMessage(
            Bundle().apply {
                putString("from", "qa-notification@test")
                putString("google.message_id", "msg-notification-1")
                putString("gcm.n.title", title)
                putString("gcm.n.body", body)
                putString("gcm.n.e", "1")
            }
        )

        service.onMessageReceived(message)
        assertTrue(
            "Notification not posted for notification payload",
            waitForNotification(title = title, body = body)
        )
    }

    private fun waitForNotification(
        title: String,
        body: String,
        timeoutMs: Long = 5_000
    ): Boolean {
        val deadline = SystemClock.elapsedRealtime() + timeoutMs
        while (SystemClock.elapsedRealtime() < deadline) {
            val found = notificationManager.activeNotifications.any { sbn ->
                val n = sbn.notification
                n.channelId == NotificationHelper.GENERAL_ANNOUNCEMENTS_CHANNEL_ID &&
                    n.extras.getString(Notification.EXTRA_TITLE) == title &&
                    n.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() == body
            }
            if (found) return true
            SystemClock.sleep(150)
        }
        return false
    }

    private fun hasPostNotificationsPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensurePostNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (hasPostNotificationsPermission()) return

        val pfd = InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .executeShellCommand(
                "pm grant ${context.packageName} ${Manifest.permission.POST_NOTIFICATIONS}"
            )
        pfd.close()
        SystemClock.sleep(300)
    }

    private fun createTestService(): MyFirebaseMessagingService {
        return MyFirebaseMessagingService().also { service ->
            val attachBaseContext = ContextWrapper::class.java.getDeclaredMethod(
                "attachBaseContext",
                Context::class.java
            )
            attachBaseContext.isAccessible = true
            attachBaseContext.invoke(service, context)
        }
    }
}
