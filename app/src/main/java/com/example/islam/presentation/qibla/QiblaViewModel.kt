package com.example.islam.presentation.qibla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.services.CompassData
import com.example.islam.services.CompassTracker
import com.example.islam.utils.QiblaCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class QiblaUiState(
    val compass: CompassData? = null,
    val hasSensor: Boolean = true,
    val isLoading: Boolean = true,
    val locationName: String = "",
    val distanceKm: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class QiblaViewModel @Inject constructor(
    private val compassTracker: CompassTracker,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    // SharingStarted.WhileSubscribed(5000) özelliği Compass ekranı arka plana
    // atıldığında veya kapatıldığında donanım sensörünü otomatik olarak serbest bırakır,
    // batarya sızıntılarını (memory/battery leak) tamamen önler.
    val uiState: StateFlow<QiblaUiState> = prefsDataStore.userPreferences
        .flatMapLatest { prefs ->
            compassTracker.track(prefs.latitude, prefs.longitude)
                .map { compassData ->
                    QiblaUiState(
                        compass = compassData,
                        hasSensor = true,
                        isLoading = false,
                        locationName = "${prefs.city}, ${prefs.country}",
                        distanceKm = QiblaCalculator.distanceToKaabaKm(prefs.latitude, prefs.longitude)
                    )
                }
                .catch { 
                    emit(QiblaUiState(
                        hasSensor = false, 
                        isLoading = false,
                        locationName = "${prefs.city}, ${prefs.country}",
                        distanceKm = QiblaCalculator.distanceToKaabaKm(prefs.latitude, prefs.longitude)
                    ))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QiblaUiState()
        )
}
