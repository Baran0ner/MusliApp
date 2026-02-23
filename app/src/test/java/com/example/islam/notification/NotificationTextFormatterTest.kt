package com.example.islam.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationTextFormatterTest {

    @Test
    fun `prayer title includes name when personalization enabled`() {
        val text = NotificationTextFormatter.prayerTitle(
            prayerName = "Öğle",
            displayName = "Baran",
            personalizedNotificationsEnabled = true
        )

        assertEquals("Baran, Öğle Vakti", text)
    }

    @Test
    fun `prayer title falls back when personalization disabled`() {
        val text = NotificationTextFormatter.prayerTitle(
            prayerName = "Öğle",
            displayName = "Baran",
            personalizedNotificationsEnabled = false
        )

        assertEquals("Öğle Vakti", text)
    }

    @Test
    fun `announcement body prefixes name when personalization enabled`() {
        val body = NotificationTextFormatter.announcementBody(
            body = "Yeni içerikler hazır.",
            displayName = "Ahmad",
            personalizedNotificationsEnabled = true
        )

        assertEquals("Ahmad, Yeni içerikler hazır.", body)
    }
}

