package com.example.islam.domain.utils

import android.location.Location

/**
 * Cihazın mevcut konumunu döndüren domain katmanı arayüzü.
 *
 * - İzin kontrolü implementasyon tarafındadır; izin yoksa **null** döner.
 * - Suspend fonksiyon olduğu için IO bloğuna gerek yoktur;
 *   FusedLocationProviderClient Task'ını coroutine'e köprüler.
 */
interface LocationTracker {
    suspend fun getCurrentLocation(): Location?
}
