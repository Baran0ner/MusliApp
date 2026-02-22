package com.example.islam.presentation.dhikr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.domain.model.Dhikr
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
import javax.inject.Inject

data class DhikrUiState(
    val dhikrList: List<Dhikr> = emptyList(),
    val selectedDhikr: Dhikr? = null,
    val cycleCount: Int = 0,            // kaç tam devir tamamlandı
    val isCelebrating: Boolean = false  // devir tamamlandığında kutlama animasyonu
)

@HiltViewModel
class DhikrViewModel @Inject constructor(
    private val getDhikrListUseCase: GetDhikrListUseCase,
    private val incrementDhikrUseCase: IncrementDhikrUseCase,
    private val resetDhikrUseCase: ResetDhikrUseCase,
    private val repository: DhikrRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DhikrUiState())
    val uiState: StateFlow<DhikrUiState> = _uiState.asStateFlow()

    init {
        seedAndLoad()
    }

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

    fun increment() {
        val dhikr = _uiState.value.selectedDhikr ?: return
        val nextCount = dhikr.count + 1

        viewModelScope.launch {
            incrementDhikrUseCase(dhikr.id)

            // Hedef sayısına ulaşıldığında otomatik devir
            if (nextCount >= dhikr.targetCount) {
                _uiState.update { it.copy(isCelebrating = true) }
                delay(700)
                resetDhikrUseCase(dhikr.id)
                _uiState.update {
                    it.copy(
                        cycleCount = it.cycleCount + 1,
                        isCelebrating = false
                    )
                }
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
}
