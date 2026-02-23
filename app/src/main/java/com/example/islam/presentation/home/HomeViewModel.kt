package com.example.islam.presentation.home

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.core.util.DateUtil
import com.example.islam.core.util.DateUtil.cleanTime
import com.example.islam.core.util.Resource
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.domain.model.DailyQuote
import com.example.islam.domain.model.PrayerPhase
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.model.UserPreferences
import com.example.islam.domain.model.WeekDay
import com.example.islam.domain.repository.PrayerHistoryRepository
import com.example.islam.domain.repository.TimeTickerRepository
import com.example.islam.domain.utils.LocationTracker
import com.example.islam.domain.usecase.prayer.GetNextPrayerUseCase
import com.example.islam.domain.usecase.prayer.GetPrayerTimesUseCase
import com.example.islam.domain.usecase.prayer.NextPrayer
import com.example.islam.domain.usecase.quote.GetDailyQuoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val prayerTime: PrayerTime? = null,
    val nextPrayer: NextPrayer? = null,
    val countdownText: String = "00:00:00",
    val currentTimeText: String = "",
    val todayDateText: String = "",
    val error: String? = null,
    val userPreferences: UserPreferences = UserPreferences(),
    val dailyQuote: DailyQuote? = null,
    /** true olduğunda namaz vakitleri yüklenir; false iken izin ekranı gösterilir */
    val permissionsGranted: Boolean = false,
    /** Ardışık tamamlanmış namaz gün sayısı */
    val prayerStreak: Int = 0,
    /** Günlük hedef (1..5) */
    val dailyPrayerGoal: Int = 5,
    /** Bugün tamamlanan vakit sayısı */
    val completedPrayersToday: Int = 0,
    /** Home streak kartı haftalık gün maskesi (Mon..Sun => bit 0..6) */
    val homeStreakWeekMask: Int = 0,
    /** Kullanıcının tercih ettiği görünen adı (opsiyonel). */
    val displayName: String = "",
    /** Home selamlama/tebrik metinlerinde kişiselleştirme açık mı. */
    val personalizedAddressingEnabled: Boolean = true,
    /** Home'daki isim istem kartı kullanıcı tarafından kapatıldı mı. */
    val namePromptDismissed: Boolean = false,
    /** Onboarding tamamlandı bilgisi. */
    val onboardingCompleted: Boolean = false,
    /** Home'da isim istem mini kartı gösterilsin mi. */
    val showNamePromptCard: Boolean = false,
    /** Ramazan başlangıcına kalan gün; null = zaten Ramazan'dayız veya hesap dışı */
    val daysToRamadan: Int? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase,
    private val getNextPrayerUseCase: GetNextPrayerUseCase,
    private val getDailyQuoteUseCase: GetDailyQuoteUseCase,
    private val prayerHistoryRepository: PrayerHistoryRepository,
    private val prefsDataStore: UserPreferencesDataStore,
    private val timeTickerRepository: TimeTickerRepository,
    private val locationTracker: LocationTracker,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            currentTimeText = DateUtil.formatTimeNow(),
            todayDateText = DateUtil.formatDateLong(),
            // İzinler zaten verilmişse doğrudan başlat — ekranda yanıp sönme olmaz
            permissionsGranted = appContext.areAllPermissionsGranted(),
            // Günlük ayet/hadis init'te hemen yüklenir (senkron, IO yok)
            dailyQuote = getDailyQuoteUseCase(),
            daysToRamadan = calculateDaysToRamadan()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /** Gökyüzü Yansımaları: Günün vaktine göre arka plan fazı (2–3 sn yumuşak geçiş için UI dinler). */
    private val _currentPrayerPhase = MutableStateFlow(PrayerPhase.NIGHT)
    val currentPrayerPhase: StateFlow<PrayerPhase> = _currentPrayerPhase.asStateFlow()

    private var observersStarted = false
    private var lastObservedDate: LocalDate = LocalDate.now()
    private var lastObservedTimezoneId: String = TimeZone.getDefault().id
    private var dateBoundaryRefreshJob: Job? = null
    private val prayerTimesLoadMutex = Mutex()

    init {
        viewModelScope.launch { prefsDataStore.ensureStreakUpToDate() }
        if (_uiState.value.permissionsGranted) startObserversIfNeeded()
        startCountdownTicker()
    }

    /**
     * Presentation katmanı tüm izinler alındıktan sonra bu metodu çağırır.
     * İdempotent: zaten granted ise ikinci çağrı işlemsiz döner.
     */
    fun onPermissionsGranted() {
        if (_uiState.value.permissionsGranted) return
        _uiState.update { it.copy(permissionsGranted = true) }
        startObserversIfNeeded()
    }

    private fun startObserversIfNeeded() {
        if (observersStarted) return
        observersStarted = true

        viewModelScope.launch {
            prefsDataStore.userPreferences
                .onStart { emit(_uiState.value.userPreferences) }
                .collect { prefs ->
                _uiState.update {
                    val showPrompt = shouldShowNamePrompt(
                        onboardingCompleted = it.onboardingCompleted,
                        namePromptDismissed = prefs.namePromptDismissed,
                        displayName = prefs.displayName
                    )
                    it.copy(
                        userPreferences = prefs,
                        dailyPrayerGoal = prefs.dailyPrayerGoal,
                        displayName = prefs.displayName,
                        personalizedAddressingEnabled = prefs.personalizedAddressingEnabled,
                        namePromptDismissed = prefs.namePromptDismissed,
                        showNamePromptCard = showPrompt
                    )
                }
            }
        }
        viewModelScope.launch {
            prefsDataStore.onboardingCompleted.collect { completed ->
                _uiState.update {
                    it.copy(
                        onboardingCompleted = completed,
                        showNamePromptCard = shouldShowNamePrompt(
                            onboardingCompleted = completed,
                            namePromptDismissed = it.namePromptDismissed,
                            displayName = it.displayName
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            prefsDataStore.userPreferences
                .distinctUntilChangedBy {
                    PrayerTimesRequestKey(
                        city = it.city,
                        country = it.country,
                        method = it.calculationMethod,
                        school = it.school,
                        useGps = it.useGps
                    )
                }
                .collect { prefs ->
                    loadPrayerTimes(prefs)
                }
        }
        viewModelScope.launch {
            prefsDataStore.prayerStreak.collect { streak ->
                _uiState.update { it.copy(prayerStreak = streak) }
            }
        }
        viewModelScope.launch {
            prefsDataStore.completedPrayersToday.collect { completed ->
                _uiState.update { it.copy(completedPrayersToday = completed.size) }
            }
        }
        viewModelScope.launch {
            combine(
                prayerHistoryRepository.getLast7Days(),
                prefsDataStore.userPreferences.map { it.dailyPrayerGoal.coerceIn(1, 5) }
            ) { days, goal ->
                buildWeeklyPrayerMask(normalizeWeeklyDays(days), goal)
            }.collect { mask ->
                _uiState.update { it.copy(homeStreakWeekMask = mask) }
            }
        }
    }

    private suspend fun loadPrayerTimes(prefs: UserPreferences) = prayerTimesLoadMutex.withLock {
        _uiState.update { it.copy(isLoading = true, error = null) }

        // GPS modu aktifse konumu al ve DataStore'a yaz (Kıble hesabı için)
        if (prefs.useGps) {
            locationTracker.getCurrentLocation()?.let { loc ->
                prefsDataStore.updateCoordinates(loc.latitude, loc.longitude)
            }
        }

        when (val result = getPrayerTimesUseCase(
            city   = prefs.city,
            country = prefs.country,
            method = prefs.calculationMethod,
            school = prefs.school
        )) {
            is Resource.Success -> {
                val pt   = result.data
                val next = getNextPrayerUseCase(pt)
                _uiState.update {
                    val stableNextPrayer = if (
                        it.nextPrayer?.prayer == next.prayer &&
                        it.nextPrayer?.timeString == next.timeString
                    ) it.nextPrayer else next
                    it.copy(
                        isLoading     = false,
                        prayerTime    = pt,
                        nextPrayer    = stableNextPrayer,
                        countdownText = DateUtil.formatCountdown(next.millisUntil)
                    )
                }
                _currentPrayerPhase.value = computePrayerPhase(pt)
            }
            is Resource.Error -> {
                _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
            is Resource.Loading -> Unit
        }
    }

    private fun startCountdownTicker() {
        viewModelScope.launch {
            timeTickerRepository.secondTicker().collect {
                val nowDate = Date()
                maybeRefreshOnDateBoundary()
                val currentTime = DateUtil.formatTimeNow(nowDate)
                val currentDateText = DateUtil.formatDateLong(nowDate)
                val pt = _uiState.value.prayerTime

                if (pt != null) {
                    // Her tikte sistem saatinden tekrar hesapla: drift oluşmaz.
                    val refreshedNext = getNextPrayerUseCase(pt)
                    _uiState.update {
                        val stableNextPrayer = if (
                            it.nextPrayer?.prayer == refreshedNext.prayer &&
                            it.nextPrayer?.timeString == refreshedNext.timeString
                        ) it.nextPrayer else refreshedNext
                        it.copy(
                            currentTimeText = currentTime,
                            todayDateText = currentDateText,
                            nextPrayer = stableNextPrayer,
                            countdownText = DateUtil.formatCountdown(refreshedNext.millisUntil)
                        )
                    }
                    _currentPrayerPhase.value = computePrayerPhase(pt)
                } else {
                    _uiState.update {
                        it.copy(
                            currentTimeText = currentTime,
                            todayDateText = currentDateText
                        )
                    }
                }
            }
        }
    }

    private fun maybeRefreshOnDateBoundary() {
        val currentDate = LocalDate.now()
        val currentTimezoneId = TimeZone.getDefault().id
        val dateChanged = currentDate != lastObservedDate
        val timezoneChanged = currentTimezoneId != lastObservedTimezoneId

        if (!dateChanged && !timezoneChanged) return

        lastObservedDate = currentDate
        lastObservedTimezoneId = currentTimezoneId

        if (dateBoundaryRefreshJob?.isActive == true) return

        dateBoundaryRefreshJob = viewModelScope.launch {
            prefsDataStore.ensureStreakUpToDate()
            _uiState.update {
                it.copy(
                    dailyQuote = getDailyQuoteUseCase(),
                    daysToRamadan = calculateDaysToRamadan()
                )
            }
            if (_uiState.value.permissionsGranted) {
                loadPrayerTimes(_uiState.value.userPreferences)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch { loadPrayerTimes(_uiState.value.userPreferences) }
    }

    fun saveDisplayName(name: String) {
        viewModelScope.launch {
            prefsDataStore.updateDisplayName(name)
            prefsDataStore.setNamePromptDismissed(true)
        }
    }

    fun dismissNamePromptCard() {
        viewModelScope.launch { prefsDataStore.setNamePromptDismissed(true) }
    }

    /**
     * Haftalık Room verisini Mon..Sun bitmask'e çevirir.
     * Bir gün "tamamlandı" sayılması için prayedCount >= dailyGoal olmalıdır.
     */
    private fun buildWeeklyPrayerMask(days: List<WeekDay>, dailyGoal: Int): Int {
        var mask = 0
        val goal = dailyGoal.coerceIn(1, 5)
        days.forEach { day ->
            val index = runCatching { dayIndex(LocalDate.parse(day.date).dayOfWeek) }.getOrNull() ?: return@forEach
            if (day.history.prayedCount >= goal) {
                mask = mask or (1 shl index)
            }
        }
        return mask
    }

    private fun normalizeWeeklyDays(days: List<WeekDay>): List<WeekDay> {
        if (days.isEmpty()) return defaultWeeklyDays()
        if (days.size >= 7) return days.takeLast(7)

        val byDate = days.associateBy { it.date }
        return defaultWeeklyDays().map { fallback -> byDate[fallback.date] ?: fallback }
    }

    private fun defaultWeeklyDays(today: LocalDate = LocalDate.now()): List<WeekDay> {
        return (6 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            WeekDay(
                date = date.toString(),
                shortName = dayShortName(date.dayOfWeek),
                history = com.example.islam.domain.model.PrayerHistory(date = date.toString())
            )
        }
    }

    private fun dayShortName(dayOfWeek: DayOfWeek): String = when (dayOfWeek) {
        DayOfWeek.MONDAY -> "Pzt"
        DayOfWeek.TUESDAY -> "Sal"
        DayOfWeek.WEDNESDAY -> "Çar"
        DayOfWeek.THURSDAY -> "Per"
        DayOfWeek.FRIDAY -> "Cum"
        DayOfWeek.SATURDAY -> "Cmt"
        DayOfWeek.SUNDAY -> "Paz"
    }

    private fun dayIndex(dayOfWeek: DayOfWeek): Int = when (dayOfWeek) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }

    private fun shouldShowNamePrompt(
        onboardingCompleted: Boolean,
        namePromptDismissed: Boolean,
        displayName: String
    ): Boolean = onboardingCompleted && !namePromptDismissed && displayName.isBlank()

    /**
     * Ramazan'ın başlangıcına kalan gün sayısını hesaplar.
     * Yaklaşık Ramazan başlangıç tarihleri (Umm al-Qura takvimi).
     * null döndürüyorsa zaten Ramazan'dayız veya bir sonraki bilinen tarih yok.
     */
    private fun calculateDaysToRamadan(): Int? {
        // (yıl, ay, gün) — Miladi, 1-indexed ay
        val ramadanStarts = listOf(
            Triple(2026, 2, 28),  // Ramazan 1447
            Triple(2027, 2, 17),  // Ramazan 1448 (yaklaşık)
            Triple(2028, 2,  6),  // Ramazan 1449 (yaklaşık)
            Triple(2029, 1, 26),  // Ramazan 1450 (yaklaşık)
        )
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        for ((y, m, d) in ramadanStarts) {
            val ramadan = Calendar.getInstance().apply {
                set(y, m - 1, d, 0, 0, 0); set(Calendar.MILLISECOND, 0)
            }
            val diffDays = ((ramadan.timeInMillis - today.timeInMillis) / 86_400_000L).toInt()
            if (diffDays >= 0) return diffDays
        }
        return null
    }

    /**
     * Mevcut saati namaz vakitleriyle kıyaslayarak şu anki [PrayerPhase] değerini döndürür.
     * DAWN: fajr–dhuhr, NOON: dhuhr–asr, AFTERNOON: asr–maghrib, SUNSET: maghrib–isha, NIGHT: isha–fajr.
     */
    private fun computePrayerPhase(pt: PrayerTime): PrayerPhase {
        val now = Calendar.getInstance()
        val fajrCal = DateUtil.todayCalendarAt(pt.fajr.cleanTime())
        val dhuhrCal = DateUtil.todayCalendarAt(pt.dhuhr.cleanTime())
        val asrCal = DateUtil.todayCalendarAt(pt.asr.cleanTime())
        val maghribCal = DateUtil.todayCalendarAt(pt.maghrib.cleanTime())
        val ishaCal = DateUtil.todayCalendarAt(pt.isha.cleanTime())

        return when {
            now.before(fajrCal) || !now.before(ishaCal) -> PrayerPhase.NIGHT   // isha → 00:00 → fajr
            !now.before(fajrCal) && now.before(dhuhrCal) -> PrayerPhase.DAWN
            !now.before(dhuhrCal) && now.before(asrCal) -> PrayerPhase.NOON
            !now.before(asrCal) && now.before(maghribCal) -> PrayerPhase.AFTERNOON
            !now.before(maghribCal) && now.before(ishaCal) -> PrayerPhase.SUNSET
            else -> PrayerPhase.NIGHT
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // İzin kontrolü — init'te senkron çalışır, UI flash'ını önler
    // ─────────────────────────────────────────────────────────────────────────

    private fun Context.areAllPermissionsGranted(): Boolean {
        val locationOk =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED

        val notificationOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else true

        val alarmOk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(AlarmManager::class.java)).canScheduleExactAlarms()
        } else true

        return locationOk && notificationOk && alarmOk
    }

    private data class PrayerTimesRequestKey(
        val city: String,
        val country: String,
        val method: Int,
        val school: Int,
        val useGps: Boolean
    )
}
