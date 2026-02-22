package com.example.islam.presentation.settings

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.domain.model.UserPreferences
import com.example.islam.domain.utils.LocationTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

// ─── Konum durumu ─────────────────────────────────────────────────────────────

sealed class LocationStatus {
    object Idle    : LocationStatus()
    object Loading : LocationStatus()
    data class Success(val displayText: String) : LocationStatus()
    data class Error(val message: String)       : LocationStatus()
}

// ─── UI Durumu ────────────────────────────────────────────────────────────────

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val locationStatus: LocationStatus = LocationStatus.Idle
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsDataStore: UserPreferencesDataStore,
    private val locationTracker: LocationTracker,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsDataStore.userPreferences.collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }
    }

    // ── GPS Konum ─────────────────────────────────────────────────────────────

    fun fetchGpsLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(locationStatus = LocationStatus.Loading) }
            try {
                val location = locationTracker.getCurrentLocation()
                if (location == null) {
                    _uiState.update {
                        it.copy(locationStatus = LocationStatus.Error("Konum alınamadı"))
                    }
                    return@launch
                }

                val lat = location.latitude
                val lon = location.longitude

                // Reverse geocoding — API ≥ 33 async, eskiler sync
                val cityName = reverseGeocode(lat, lon)

                prefsDataStore.updateCoordinates(lat, lon)
                prefsDataStore.updateCity(cityName, "")

                _uiState.update {
                    it.copy(
                        locationStatus = LocationStatus.Success(
                            "$cityName  (%.4f, %.4f)".format(lat, lon)
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(locationStatus = LocationStatus.Error(e.message ?: "Bilinmeyen hata"))
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun reverseGeocode(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Non-blocking callback — Blocking çalışmaz; küçük coroutine trampolin kullan
                var result = "%.4f, %.4f".format(lat, lon)
                val latch = java.util.concurrent.CountDownLatch(1)
                geocoder.getFromLocation(lat, lon, 1) { addresses ->
                    result = addresses.firstOrNull()?.let { addr ->
                        addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    } ?: result
                    latch.countDown()
                }
                latch.await(3, java.util.concurrent.TimeUnit.SECONDS)
                result
            } else {
                geocoder.getFromLocation(lat, lon, 1)
                    ?.firstOrNull()
                    ?.let { it.locality ?: it.subAdminArea ?: it.adminArea }
                    ?: "%.4f, %.4f".format(lat, lon)
            }
        } catch (e: Exception) {
            "%.4f, %.4f".format(lat, lon)
        }
    }

    // ── Tema ─────────────────────────────────────────────────────────────────

    fun setAppTheme(theme: Int) {
        viewModelScope.launch { prefsDataStore.updateAppTheme(theme) }
    }

    // Eski API — geriye dönük uyumluluk için tutuldu
    fun setDarkTheme(dark: Boolean) = setAppTheme(if (dark) 1 else 0)

    // ── Bildirimler ───────────────────────────────────────────────────────────

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { prefsDataStore.updateNotifications(enabled) }
    }

    fun setPrayerNotifType(prayerId: String, type: Int) {
        viewModelScope.launch { prefsDataStore.updatePrayerNotifType(prayerId, type) }
    }

    // ── Hicri Takvim ─────────────────────────────────────────────────────────

    fun setHijriOffset(offset: Int) {
        viewModelScope.launch { prefsDataStore.updateHijriOffset(offset) }
    }

    // ── Hesaplama & Mezhep ────────────────────────────────────────────────────

    fun setCalculationMethod(method: Int) {
        viewModelScope.launch { prefsDataStore.updateCalculationMethod(method) }
    }

    fun setSchool(school: Int) {
        viewModelScope.launch { prefsDataStore.updateSchool(school) }
    }

    // ── Dil ───────────────────────────────────────────────────────────────────

    fun setLanguage(language: String) {
        viewModelScope.launch { prefsDataStore.updateLanguage(language) }
    }

    // ── Konum yardımcıları (mantıksal erişim için) ───────────────────────────

    /** Mevcut kaydedilmiş konumu "Şehir (lat, lon)" formatında döner. */
    val savedLocationText: String
        get() = _uiState.value.preferences.let { p ->
            if (p.city.isNotBlank() && p.city != "Istanbul") {
                "${p.city}  (%.4f, %.4f)".format(p.latitude, p.longitude)
            } else {
                "%.4f, %.4f".format(p.latitude, p.longitude)
            }
        }
}
