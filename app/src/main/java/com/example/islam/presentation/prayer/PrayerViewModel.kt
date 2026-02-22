package com.example.islam.presentation.prayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.core.util.Resource
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.domain.model.Prayer
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.model.PrayerType
import com.example.islam.domain.model.UserPreferences
import com.example.islam.domain.model.WeekDay
import com.example.islam.domain.model.timeFor
import com.example.islam.data.repository.FirebaseRepository
import com.example.islam.domain.repository.PrayerHistoryRepository
import com.example.islam.domain.usecase.prayer.GetPrayerTimesUseCase
import com.example.islam.domain.usecase.prayer.GetNextPrayerUseCase
import com.example.islam.domain.usecase.prayer.NextPrayer
import com.example.islam.core.util.DateUtil.cleanTime
import com.example.islam.core.util.DateUtil.formatCountdown
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class PrayerUiState(
    val isLoading        : Boolean          = false,
    val prayerTime       : PrayerTime?      = null,
    val currentPrayer    : Prayer?          = null,
    val nextPrayer       : NextPrayer?      = null,
    val countdownText    : String           = "00:00:00",
    val error            : String?          = null,
    val userPreferences  : UserPreferences  = UserPreferences(),
    val completedPrayers : Set<String>      = emptySet(),
    val weeklyHistory   : List<WeekDay>     = emptyList(),
    val selectedDay      : WeekDay?         = null,
    val showDaySheet     : Boolean          = false
)

@HiltViewModel
class PrayerViewModel @Inject constructor(
    private val getPrayerTimesUseCase  : GetPrayerTimesUseCase,
    private val getNextPrayerUseCase   : GetNextPrayerUseCase,
    private val prefsDataStore         : UserPreferencesDataStore,
    private val prayerHistoryRepository: PrayerHistoryRepository,
    private val firebaseRepository     : FirebaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    private val trackablePrayers = listOf(
        Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA
    ).map { it.name }

    init {
        viewModelScope.launch {
            prefsDataStore.userPreferences.collect { prefs ->
                _uiState.update { it.copy(userPreferences = prefs) }
                loadTodaysPrayers(prefs)
            }
        }
        viewModelScope.launch {
            prefsDataStore.completedPrayersToday.collect { completed ->
                _uiState.update { it.copy(completedPrayers = completed) }
            }
        }
        viewModelScope.launch {
            prayerHistoryRepository.getLast7Days().collect { days ->
                _uiState.update { state -> 
                    val newSelectedDay = state.selectedDay?.let { currentSelected ->
                        days.find { it.date == currentSelected.date }
                    } ?: state.selectedDay
                    state.copy(weeklyHistory = days, selectedDay = newSelectedDay) 
                }
            }
        }
        startCountdownTicker()
    }

    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val next = _uiState.value.nextPrayer ?: continue
                val remaining = next.millisUntil - 1_000L
                if (remaining > 0) {
                    _uiState.update {
                        it.copy(
                            nextPrayer = next.copy(millisUntil = remaining),
                            countdownText = formatCountdown(remaining)
                        )
                    }
                } else {
                    _uiState.value.prayerTime?.let { pt ->
                        val newNext = getNextPrayerUseCase(pt)
                        _uiState.update {
                            it.copy(
                                nextPrayer = newNext,
                                countdownText = formatCountdown(newNext.millisUntil),
                                currentPrayer = determineCurrentPrayer(pt)
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Bugünkü namaz checkbox (DataStore tabanlı) ────────────────────────────
    fun togglePrayerCompleted(prayer: Prayer) {
        viewModelScope.launch {
            prefsDataStore.togglePrayerCompleted(prayer.name, trackablePrayers)

            // Sync with Room DB
            val todayDate = java.time.LocalDate.now().toString()
            val type = when (prayer) {
                Prayer.FAJR    -> PrayerType.FAJR
                Prayer.DHUHR   -> PrayerType.DHUHR
                Prayer.ASR     -> PrayerType.ASR
                Prayer.MAGHRIB -> PrayerType.MAGHRIB
                Prayer.ISHA    -> PrayerType.ISHA
                else           -> null
            }
            if (type != null) {
                prayerHistoryRepository.togglePrayerStatus(todayDate, type)
            }

            // Kullanıcı giriş yapmışsa güncel streak'i Firestore'a yaz
            if (firebaseRepository.isSignedIn) {
                val streak = prefsDataStore.prayerStreak.first()
                firebaseRepository.syncStreak(streak)
            }
        }
    }

    // ── Haftalık geçmiş toggle (Room DB tabanlı) ──────────────────────────────
    fun toggleHistoryPrayer(date: String, prayerType: PrayerType) {
        viewModelScope.launch {
            prayerHistoryRepository.togglePrayerStatus(date, prayerType)
            
            // Sync with DataStore if it's today
            val todayDate = java.time.LocalDate.now().toString()
            if (date == todayDate) {
                val prayerName = when (prayerType) {
                    PrayerType.FAJR -> Prayer.FAJR.name
                    PrayerType.DHUHR -> Prayer.DHUHR.name
                    PrayerType.ASR -> Prayer.ASR.name
                    PrayerType.MAGHRIB -> Prayer.MAGHRIB.name
                    PrayerType.ISHA -> Prayer.ISHA.name
                }
                prefsDataStore.togglePrayerCompleted(prayerName, trackablePrayers)
            }
        }
    }

    // ── BottomSheet açma / kapama ─────────────────────────────────────────────
    fun selectDay(day: WeekDay) {
        _uiState.update { it.copy(selectedDay = day, showDaySheet = true) }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(showDaySheet = false, selectedDay = null) }
    }

    // ── Namaz vakitleri yükleme ───────────────────────────────────────────────
    private suspend fun loadTodaysPrayers(prefs: UserPreferences) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        when (val result = getPrayerTimesUseCase(prefs.city, prefs.country, prefs.calculationMethod)) {
            is Resource.Success -> {
                val pt = result.data
                val next = getNextPrayerUseCase(pt)
                _uiState.update {
                    it.copy(
                        isLoading      = false,
                        prayerTime     = pt,
                        currentPrayer  = determineCurrentPrayer(pt),
                        nextPrayer    = next,
                        countdownText = formatCountdown(next.millisUntil)
                    )
                }
            }
            is Resource.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            is Resource.Loading -> Unit
        }
    }

    private fun determineCurrentPrayer(pt: PrayerTime): Prayer? {
        val now = Calendar.getInstance()
        val orderedPrayers = listOf(
            Prayer.IMSAK, Prayer.FAJR, Prayer.SUNRISE,
            Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA
        )
        var current: Prayer? = null
        for (prayer in orderedPrayers) {
            val timeStr = pt.timeFor(prayer).cleanTime()
            val parts = timeStr.split(":")
            if (parts.size < 2) continue
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                set(Calendar.MINUTE, parts[1].toInt())
                set(Calendar.SECOND, 0)
            }
            if (cal.before(now) || cal == now) current = prayer
        }
        return current
    }

    fun refresh() {
        viewModelScope.launch { loadTodaysPrayers(_uiState.value.userPreferences) }
    }
}
