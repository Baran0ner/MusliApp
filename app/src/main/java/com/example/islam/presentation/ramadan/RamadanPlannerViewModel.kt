package com.example.islam.presentation.ramadan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.domain.model.PrayerType
import com.example.islam.domain.repository.RamadanPlannerRepository
import com.example.islam.domain.usecase.prayer.GetPrayerTimesUseCase
import com.example.islam.data.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class RamadanPlannerDayUi(
    val dateIso: String,
    val day: Int,
    val fajr: String,
    val maghrib: String,
    val hijriDate: String,
    val fastDone: Boolean,
    val fajrDone: Boolean,
    val dhuhrDone: Boolean,
    val asrDone: Boolean,
    val maghribDone: Boolean,
    val ishaDone: Boolean,
    val prayedCount: Int,
    val dailyGoal: Int,
    val goalReached: Boolean,
    val isToday: Boolean
)

data class RamadanPlannerUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val days: List<RamadanPlannerDayUi> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val cityLabel: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RamadanPlannerViewModel @Inject constructor(
    private val repository: RamadanPlannerRepository,
    private val prefsDataStore: UserPreferencesDataStore,
    private val getPrayerTimesUseCase: GetPrayerTimesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RamadanPlannerUiState())
    val uiState: StateFlow<RamadanPlannerUiState> = _uiState.asStateFlow()

    private val selectedMonth = MutableStateFlow(YearMonth.now())

    init {
        observeMonthData()
    }

    fun previousMonth() {
        selectedMonth.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        selectedMonth.update { it.plusMonths(1) }
    }

    fun toggleFast(dateIso: String, checked: Boolean) {
        viewModelScope.launch {
            repository.setFastStatus(dateIso, checked)
        }
    }

    fun togglePrayer(dateIso: String, prayerType: PrayerType, checked: Boolean) {
        viewModelScope.launch {
            repository.setPrayerCompletion(dateIso, prayerType, checked)
        }
    }

    fun completeToday() {
        val today = LocalDate.now().toString()
        viewModelScope.launch {
            repository.setFastStatus(today, true)
            PrayerType.entries.forEach { prayer ->
                repository.setPrayerCompletion(today, prayer, true)
            }
        }
    }

    private fun observeMonthData() {
        viewModelScope.launch {
            combine(prefsDataStore.userPreferences, selectedMonth) { prefs, month ->
                prefs to month
            }.flatMapLatest { (prefs, month) ->
                flow {
                    _uiState.update {
                        it.copy(
                            selectedMonth = month,
                            isLoading = true,
                            error = null,
                            cityLabel = "${prefs.city}, ${prefs.country}"
                        )
                    }

                    val firstDayApi = "%02d-%02d-%04d".format(1, month.monthValue, month.year)
                    // Ay görünümünü offline-first doldurmak için aylık cache'i warm-up eder.
                    getPrayerTimesUseCase(
                        city = prefs.city,
                        country = prefs.country,
                        method = prefs.calculationMethod,
                        school = prefs.school,
                        date = firstDayApi
                    )

                    emitAll(
                        combine(
                            repository.observeMonth(
                                city = prefs.city,
                                country = prefs.country,
                                month = month.monthValue,
                                year = month.year,
                                method = prefs.calculationMethod,
                                school = prefs.school
                            ),
                            repository.observeIbadahProgress(
                                month = month.monthValue,
                                year = month.year
                            )
                        ) { plans, progress ->
                            val planByDate = plans.associateBy { it.dateIso }
                            val progressByDate = progress.associateBy { it.dateIso }
                            val today = LocalDate.now().toString()

                            (1..month.lengthOfMonth()).map { dayOfMonth ->
                                val dateIso = month.atDay(dayOfMonth).toString()
                                val dayPlan = planByDate[dateIso]
                                val dayProgress = progressByDate[dateIso]
                                val prayedCount = dayProgress?.prayedCount ?: 0
                                val goal = prefs.dailyPrayerGoal.coerceIn(1, 5)
                                RamadanPlannerDayUi(
                                    dateIso = dateIso,
                                    day = dayOfMonth,
                                    fajr = dayPlan?.fajr ?: "--:--",
                                    maghrib = dayPlan?.maghrib ?: "--:--",
                                    hijriDate = dayPlan?.hijriDate ?: "—",
                                    fastDone = dayProgress?.fastDone ?: false,
                                    fajrDone = dayProgress?.fajrDone ?: false,
                                    dhuhrDone = dayProgress?.dhuhrDone ?: false,
                                    asrDone = dayProgress?.asrDone ?: false,
                                    maghribDone = dayProgress?.maghribDone ?: false,
                                    ishaDone = dayProgress?.ishaDone ?: false,
                                    prayedCount = prayedCount,
                                    dailyGoal = goal,
                                    goalReached = prayedCount >= goal,
                                    isToday = dateIso == today
                                )
                            }
                        }
                    )
                }
            }.collect { days ->
                _uiState.update {
                    it.copy(
                        selectedMonth = selectedMonth.value,
                        days = days,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }
}
