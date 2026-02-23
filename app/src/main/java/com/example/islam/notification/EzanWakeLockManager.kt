package com.example.islam.notification

import android.content.Context
import android.os.PowerManager
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Doze aninda Receiver -> ForegroundService gecisinde CPU'nun tekrar uykuya dalmasini engeller.
 * WakeLock 5 saniye timeout ile alinir; servis isini bitirdiginde manuel release de eder.
 */
object EzanWakeLockManager {

    private const val WAKELOCK_TAG_PREFIX = "com.example.islam:ezan_wakelock:"
    private val wakeLocks = ConcurrentHashMap<String, PowerManager.WakeLock>()

    fun acquire(context: Context, timeoutMs: Long = 5_000L): String? {
        val powerManager = context.getSystemService(PowerManager::class.java) ?: return null
        val token = UUID.randomUUID().toString()
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "$WAKELOCK_TAG_PREFIX$token"
        ).apply {
            setReferenceCounted(false)
        }

        return runCatching {
            wakeLock.acquire(timeoutMs)
            wakeLocks[token] = wakeLock
            token
        }.getOrNull()
    }

    fun release(token: String?) {
        if (token.isNullOrBlank()) return
        val wakeLock = wakeLocks.remove(token) ?: return
        runCatching {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
}
