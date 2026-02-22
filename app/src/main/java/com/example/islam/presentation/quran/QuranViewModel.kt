package com.example.islam.presentation.quran

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.data.repository.QuranRepository
import com.example.islam.domain.model.JuzListModel
import com.example.islam.domain.model.SurahListModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuranListUiState(
    val surahs: List<SurahListModel> = emptyList(),
    val juzs: List<JuzListModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class QuranViewModel @Inject constructor(
    private val repository: QuranRepository,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuranListUiState())
    val uiState: StateFlow<QuranListUiState> = _uiState.asStateFlow()

    val lastRead: StateFlow<UserPreferencesDataStore.LastRead?> = prefsDataStore.lastRead
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val bookmarkedSurahIds: StateFlow<Set<Int>> = prefsDataStore.bookmarkedSurahIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleBookmark(surahNumber: Int) {
        viewModelScope.launch {
            prefsDataStore.toggleBookmarkSurah(surahNumber)
        }
    }

    init {
        // Statik liste ile ekran hemen dolu açılır; API arka planda günceller
        _uiState.value = _uiState.value.copy(
            surahs = repository.getStaticSurahList(),
            juzs = repository.getJuzList(),
            isLoading = false,
            error = null
        )
        loadSurahs(refresh = false)
    }

    private var loadJob: Job? = null

    /**
     * @param refresh true = "Tekrar Dene" ile çağrıldı; API hata verirse kullanıcıya hata gösterilir
     */
    fun loadSurahs(refresh: Boolean = false) {
        loadJob?.cancel()
        if (refresh) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        }
        loadJob = viewModelScope.launch {
            repository.getSurahList()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(
                        surahs = list,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = if (refresh) (e.message ?: "Sureler yüklenemedi. Tekrar deneyin.") else null
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
