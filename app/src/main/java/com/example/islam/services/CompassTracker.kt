package com.example.islam.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// Kabe koordinatları (sabit)
// ─────────────────────────────────────────────────────────────────────────────
private const val KAABA_LAT = 21.4225
private const val KAABA_LON = 39.8262

// Düşük geçirgen filtre katsayısı — 0 = değişim yok, 1 = ham değer
private const val ALPHA = 0.15f

// Küçük açı değişimlerini (< 1°) filtrele — Flow'da gereksiz emit'i önler
private const val MIN_ANGLE_CHANGE = 1

/**
 * Flow aracılığıyla yayınlanan anlık pusula verisi.
 *
 * @param azimuth       Cihazın baktığı yön: 0–360°, saat yönünde, 0 = Kuzey
 * @param qiblaAngle    Kabe'nin coğrafi kuzeyden açısı: 0–360°, kullanıcı konumuna göre
 * @param bearingToQibla Kıbleye dönmek için cihazın ne kadar döneceği: 0–360°
 *                       (saat yönünde pozitif, saatin tersi negatif → UI'da okun açısı)
 */
data class CompassData(
    val azimuth: Float,
    val qiblaAngle: Float,
    val bearingToQibla: Float
)

/**
 * Manyetometre + ivmeölçer sensörlerini Kotlin Flow ile saran yardımcı sınıf.
 *
 * ### Kullanım (ViewModel'da)
 * ```kotlin
 * compassTracker
 *     .track(userLat, userLon)
 *     .onEach { data -> _uiState.update { it.copy(compass = data) } }
 *     .launchIn(viewModelScope)
 * ```
 *
 * Flow, kollektör iptal edildiğinde (örn. ViewModel temizlendiğinde)
 * sensör dinleyicisini otomatik olarak kaldırır.
 */
@Singleton
class CompassTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Sensörden gelen anlık pusula verisini ve hesaplanmış kıble açısını yayınlar.
     *
     * @param userLat Kullanıcının enlem derecesi
     * @param userLon Kullanıcının boylam derecesi
     * @return Her sensör güncellemesinde [CompassData] yayınlayan sonsuz Flow.
     *         Manyetometre veya ivmeölçer yoksa Flow hata ile kapanır.
     */
    fun track(userLat: Double, userLon: Double): Flow<CompassData> {

        // Kıble açısı kullanıcı konumuna göre tek seferlik hesaplanır
        val qiblaAngle = calculateQiblaAngle(userLat, userLon)

        return callbackFlow {

            val sensorManager =
                context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magnetometer  = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            if (accelerometer == null || magnetometer == null) {
                close(IllegalStateException("Manyetometre veya ivmeölçer sensörü bulunamadı."))
                return@callbackFlow
            }

            // Her iki sensörden gerçek veri gelene kadar null — boş array ile
            // getRotationMatrix çağırmak her zaman başarısız olur ve hiç emit yapılmaz.
            var gravity: FloatArray? = null
            var geomagnetic: FloatArray? = null

            val listener = object : SensorEventListener {

                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER ->
                            gravity = lowPassFilter(
                                event.values.clone(),
                                gravity ?: FloatArray(3)
                            )

                        Sensor.TYPE_MAGNETIC_FIELD ->
                            geomagnetic = lowPassFilter(
                                event.values.clone(),
                                geomagnetic ?: FloatArray(3)
                            )
                    }

                    // Her iki sensörden de gerçek veri geldikten sonra işle
                    val g = gravity ?: return
                    val m = geomagnetic ?: return

                    val rotationMatrix = FloatArray(9)
                    val success = SensorManager.getRotationMatrix(
                        rotationMatrix, null, g, m
                    )
                    if (!success) return

                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)

                    // orientation[0] → azimut (radyan, -π … +π)
                    val azimuth = normalizeDegrees(
                        Math.toDegrees(orientation[0].toDouble()).toFloat()
                    )

                    // Kıbleye dönmek için gereken açı (saat yönünde)
                    val bearingToQibla = normalizeDegrees(qiblaAngle - azimuth)

                    trySend(CompassData(azimuth, qiblaAngle, bearingToQibla))
                }

                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
            }

            sensorManager.registerListener(
                listener, accelerometer, SensorManager.SENSOR_DELAY_UI
            )
            sensorManager.registerListener(
                listener, magnetometer, SensorManager.SENSOR_DELAY_UI
            )

            // Flow iptal edildiğinde (ViewModel silindiğinde) sensörü durdur
            awaitClose { sensorManager.unregisterListener(listener) }

        }.distinctUntilChanged { old, new ->
            // Azimut farkı eşik altındaysa yeni emit yapma (gereksiz recomposition önlenir)
            abs(old.azimuth.roundToInt() - new.azimuth.roundToInt()) < MIN_ANGLE_CHANGE &&
                    abs(old.bearingToQibla.roundToInt() - new.bearingToQibla.roundToInt()) < MIN_ANGLE_CHANGE
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Kıble açısı hesabı — Büyük Daire (Great Circle) formülü
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Verilen koordinattan Kabe'ye olan coğrafi kuzeyden açıyı (0–360°) döndürür.
     */
    private fun calculateQiblaAngle(userLat: Double, userLon: Double): Float {
        val φ1 = Math.toRadians(userLat)
        val φ2 = Math.toRadians(KAABA_LAT)
        val Δλ = Math.toRadians(KAABA_LON - userLon)

        val x = sin(Δλ) * cos(φ2)
        val y = cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(Δλ)

        return normalizeDegrees(Math.toDegrees(atan2(x, y)).toFloat())
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Yardımcı fonksiyonlar
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Eksponansiyel düşük geçirgen filtre: sensör titremesini yumuşatır.
     * output[i] = output[i] + α * (input[i] − output[i])
     */
    private fun lowPassFilter(input: FloatArray, output: FloatArray): FloatArray {
        for (i in input.indices) {
            output[i] = output[i] + ALPHA * (input[i] - output[i])
        }
        return output
    }

    /** Herhangi bir dereceyi 0–360° aralığına normalize eder. */
    private fun normalizeDegrees(degrees: Float): Float = (degrees + 360f) % 360f
}
