package com.example.islam.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.islam.core.util.Resource
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.domain.repository.PrayerTimeRepository
import com.example.islam.notification.AlarmScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class PrayerTimeUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val prefsDataStore: UserPreferencesDataStore,
    private val alarmScheduler: AlarmScheduler
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val prefs = prefsDataStore.userPreferences.first()
            val result = prayerTimeRepository.getPrayerTimes(
                city = prefs.city,
                country = prefs.country,
                method = prefs.calculationMethod
            )
            when (result) {
                is Resource.Success -> {
                    if (prefs.notificationsEnabled) {
                        alarmScheduler.schedulePrayerAlarms(result.data)
                    }
                    Result.success()
                }
                is Resource.Error -> Result.retry()
                is Resource.Loading -> Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "prayer_time_daily_update"

        /** Enqueues a periodic daily update at midnight */
        fun enqueueDailyWork(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<PrayerTimeUpdateWorker>(
                repeatInterval = 24,
                repeatIntervalTimeUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
