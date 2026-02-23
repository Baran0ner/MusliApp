package com.example.islam.presentation.dhikr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.domain.model.Dhikr
import com.example.islam.domain.model.DhikrDay
import com.example.islam.domain.model.DhikrRecord
import com.example.islam.domain.repository.DhikrHistoryRepository
import com.example.islam.domain.repository.DhikrRepository
import com.example.islam.domain.usecase.dhikr.GetDhikrListUseCase
import com.example.islam.domain.usecase.dhikr.IncrementDhikrUseCase
import com.example.islam.domain.usecase.dhikr.ResetDhikrUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class DhikrUiState(
    val dhikrList: List<Dhikr> = emptyList(),
    val selectedDhikr: Dhikr? = null,
    val cycleCount: Int = 0,
    val isCelebrating: Boolean = false,
    val weeklyDays: List<DhikrDay> = emptyList(),
    val selectedDay: DhikrDay? = null,
    val showDaySheet: Boolean = false,
    val dayDetailRecords: List<DhikrRecord> = emptyList()
)

@HiltViewModel
class DhikrViewModel @Inject constructor(
    private val getDhikrListUseCase: GetDhikrListUseCase,
    private val incrementDhikrUseCase: IncrementDhikrUseCase,
    private val resetDhikrUseCase: ResetDhikrUseCase,
    private val repository: DhikrRepository,
    private val dhikrHistoryRepository: DhikrHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DhikrUiState())
    val uiState: StateFlow<DhikrUiState> = _uiState.asStateFlow()

    init {
        seedAndLoad()
        viewModelScope.launch {
            dhikrHistoryRepository.getLast7Days().collect { days ->
                _uiState.update { it.copy(weeklyDays = days) }
            }
        }
    }

    private fun todayDate(): String = LocalDate.now().toString()

    private fun seedAndLoad() {
        viewModelScope.launch {
            repository.seedIfEmpty()
            getDhikrListUseCase().collect { list ->
                val selected = _uiState.value.selectedDhikr
                val updatedSelected = if (selected != null)
                    list.find { d -> d.id == selected.id } ?: list.firstOrNull()
                else
                    list.firstOrNull()

                _uiState.update {
                    it.copy(
                        dhikrList = list,
                        selectedDhikr = updatedSelected
                        // cycleCount ve isCelebrating korunur
                    )
                }
            }
        }
    }

    fun selectDhikr(dhikr: Dhikr) {
        _uiState.update { it.copy(selectedDhikr = dhikr, cycleCount = 0) }
    }

    private val incrementMutex = Mutex()

    fun increment() {
        // Kutlama/Reset sırasında spam tıklamalar kuyruklanıp
        // kutlama bitince hızlıca birden fazla devir atlattırmasın diye:
        // - Kilit alınamıyorsa tap'i düşür
        // - Kutlama aktifken yeni tap'leri düşür
        if (_uiState.value.isCelebrating) return
        if (!incrementMutex.tryLock()) return

        viewModelScope.launch {
            try {
                val dhikr = _uiState.value.selectedDhikr ?: return@launch
                val nextCount = dhikr.count + 1

                incrementDhikrUseCase(dhikr.id)

                if (nextCount >= dhikr.targetCount) {
                    _uiState.update { it.copy(isCelebrating = true, cycleCount = it.cycleCount + 1) }
                    dhikrHistoryRepository.saveRecord(todayDate(), dhikr.name, dhikr.targetCount)
                    resetDhikrUseCase(dhikr.id)

                    // Kilit dışında, sadece UI kutlama süresi
                    viewModelScope.launch {
                        delay(700)
                        _uiState.update { it.copy(isCelebrating = false) }
                    }
                }
            } finally {
                incrementMutex.unlock()
            }
        }
    }

    fun reset() {
        val dhikr = _uiState.value.selectedDhikr ?: return
        viewModelScope.launch {
            resetDhikrUseCase(dhikr.id)
            _uiState.update { it.copy(cycleCount = 0) }
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            resetDhikrUseCase.resetAll()
            _uiState.update { it.copy(cycleCount = 0) }
        }
    }

    fun saveCurrent() {
        val dhikr = _uiState.value.selectedDhikr ?: return
        if (dhikr.count <= 0) return
        viewModelScope.launch {
            dhikrHistoryRepository.saveRecord(todayDate(), dhikr.name, dhikr.count)
            resetDhikrUseCase(dhikr.id)
            _uiState.update { it.copy(cycleCount = 0) }
        }
    }

    fun selectDay(day: DhikrDay) {
        viewModelScope.launch {
            val records = dhikrHistoryRepository.getRecordsByDate(day.date)
            _uiState.update {
                it.copy(selectedDay = day, showDaySheet = true, dayDetailRecords = records)
            }
        }
    }

    fun dismissDaySheet() {
        _uiState.update { it.copy(showDaySheet = false, selectedDay = null, dayDetailRecords = emptyList()) }
    }
}
