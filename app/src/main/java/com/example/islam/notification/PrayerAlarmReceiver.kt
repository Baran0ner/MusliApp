package com.example.islam.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.islam.domain.model.Prayer
import com.example.islam.services.EzanForegroundService

/**
 * AlarmManager tarafından tetiklenir.
 *
 * Bildirimi doğrudan göstermek yerine [EzanForegroundService]'i başlatır;
 * servis hem foreground bildirimi hem de ezan sesini yönetir.
 *
 * Arka plan kısıtlamalarını aşmak için [ContextCompat.startForegroundService]
 * kullanılır — servis 5 saniye içinde [Service.startForeground] çağırmalıdır.
 */
class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(EXTRA_PRAYER_NAME) ?: return
        val prayerTime = intent.getStringExtra(EXTRA_PRAYER_TIME) ?: ""
        val channelId  = intent.getStringExtra(EXTRA_CHANNEL_ID)  ?: Prayer.NOTIFICATION_CHANNEL_ID

        val serviceIntent = EzanForegroundService.buildStartIntent(
            context     = context,
            prayerName  = prayerName,
            prayerTime  = prayerTime,
            channelId   = channelId
        )
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    companion object {
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_PRAYER_TIME = "extra_prayer_time"
        const val EXTRA_CHANNEL_ID  = "extra_channel_id"
    }
}
