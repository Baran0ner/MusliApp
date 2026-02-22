package com.example.islam.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Küresel trigonometri kullanarak Kıble yönünü ve mesafesini hesaplar.
 *
 * Formül: Great Circle Bearing
 *   θ = atan2( sin(Δλ)·cos(φ₂),
 *              cos(φ₁)·sin(φ₂) − sin(φ₁)·cos(φ₂)·cos(Δλ) )
 *
 * Sonuç kuzeye göre saat yönünde derece cinsinden (0–360°) döner.
 */
object QiblaCalculator {

    private const val KAABA_LAT = 21.422487
    private const val KAABA_LON = 39.826206

    /** Dünya yarıçapı (km) — Haversine formülü için. */
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Kullanıcının konumundan Kabe'ye olan yönü (Kıble açısını) hesaplar.
     *
     * @param userLat Kullanıcının enlemi (derece)
     * @param userLng Kullanıcının boylamı (derece)
     * @return Kuzeye göre saat yönünde Kıble açısı (0–360°)
     */
    fun calculateQiblaDirection(userLat: Double, userLng: Double): Float {
        val φ1 = Math.toRadians(userLat)
        val φ2 = Math.toRadians(KAABA_LAT)
        val Δλ = Math.toRadians(KAABA_LON - userLng)

        val y = sin(Δλ) * cos(φ2)
        val x = cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(Δλ)

        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad).toFloat()

        return (bearingDeg + 360f) % 360f
    }

    /**
     * Kullanıcı konumundan Kabe'ye kuş uçuşu mesafeyi (km) hesaplar (Haversine).
     *
     * @param userLat Kullanıcının enlemi (derece)
     * @param userLng Kullanıcının boylamı (derece)
     * @return Mesafe (km)
     */
    fun distanceToKaabaKm(userLat: Double, userLng: Double): Int {
        val φ1 = Math.toRadians(userLat)
        val φ2 = Math.toRadians(KAABA_LAT)
        val Δφ = Math.toRadians(KAABA_LAT - userLat)
        val Δλ = Math.toRadians(KAABA_LON - userLng)
        val a = sin(Δφ / 2) * sin(Δφ / 2) +
                cos(φ1) * cos(φ2) * sin(Δλ / 2) * sin(Δλ / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (EARTH_RADIUS_KM * c).toInt()
    }
}
