package com.example.islam.presentation.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.islam.worker.PrayerTimeUpdateWorker

/**
 * GlanceAppWidgetReceiver — sistem, bu receiver üzerinden widget güncelleme
 * olaylarını (ACTION_APPWIDGET_UPDATE, ACTION_APPWIDGET_ENABLED vb.) iletir.
 *
 * Widget ilk eklendiğinde veya sistem tarafından refresh istendiğinde
 * PrayerTimeUpdateWorker'ı tek seferlik tetikler; bu worker hem namaz
 * vakitlerini hem de widget state'ini günceller.
 */
class NextPrayerWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = NextPrayerWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        triggerWorker(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        triggerWorker(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Özel broadcast — örneğin namaz vakti güncellenince widget'ı yenile
        if (intent.action == ACTION_REFRESH_WIDGET) {
            triggerWorker(context)
        }
    }

    private fun triggerWorker(context: Context) {
        val request = OneTimeWorkRequestBuilder<PrayerTimeUpdateWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.islam.action.REFRESH_WIDGET"
    }
}
