package com.example.islam.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.example.islam.domain.utils.LocationTracker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * [LocationTracker] implementasyonu.
 *
 * - GPS varsa [Priority.PRIORITY_HIGH_ACCURACY] kullanır.
 * - Sadece ağ/WiFi izni varsa [Priority.PRIORITY_BALANCED_POWER_ACCURACY] ile devam eder.
 * - Herhangi bir konum izni yoksa `null` döner; uygulama çökmez.
 * - Task iptal edildiğinde [CancellationTokenSource] aracılığıyla kaynaklar serbest bırakılır.
 */
@Singleton
class DefaultLocationTracker @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
) : LocationTracker {

    override suspend fun getCurrentLocation(): Location? {
        val hasFine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return null

        val priority = if (hasFine)
            Priority.PRIORITY_HIGH_ACCURACY
        else
            Priority.PRIORITY_BALANCED_POWER_ACCURACY

        return suspendCancellableCoroutine { continuation ->
            val cts = CancellationTokenSource()

            fusedLocationClient
                .getCurrentLocation(priority, cts.token)
                .addOnSuccessListener { location -> continuation.resume(location) }
                .addOnFailureListener { continuation.resume(null) }
                .addOnCanceledListener { continuation.resume(null) }

            continuation.invokeOnCancellation { cts.cancel() }
        }
    }
}
