package com.example.islam.notification

object NotificationTextFormatter {

    fun prayerTitle(
        prayerName: String,
        displayName: String?,
        personalizedNotificationsEnabled: Boolean
    ): String {
        val safeName = normalizedName(displayName)
        return if (personalizedNotificationsEnabled && safeName != null) {
            "$safeName, $prayerName Vakti"
        } else {
            "$prayerName Vakti"
        }
    }

    fun prayerBody(
        prayerName: String,
        prayerTime: String,
        displayName: String?,
        personalizedNotificationsEnabled: Boolean
    ): String {
        val safeName = normalizedName(displayName)
        return if (personalizedNotificationsEnabled && safeName != null) {
            "$safeName, $prayerName namazının vakti geldi: $prayerTime"
        } else {
            "$prayerName namazının vakti geldi: $prayerTime"
        }
    }

    fun announcementBody(
        body: String,
        displayName: String?,
        personalizedNotificationsEnabled: Boolean
    ): String {
        val safeName = normalizedName(displayName)
        return if (personalizedNotificationsEnabled && safeName != null) {
            "$safeName, $body"
        } else {
            body
        }
    }

    private fun normalizedName(name: String?): String? {
        val trimmed = name?.trim().orEmpty()
        if (trimmed.length !in 2..24) return null
        return trimmed
    }
}

