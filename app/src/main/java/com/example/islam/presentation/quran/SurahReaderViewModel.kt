package com.example.islam.presentation.quran

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.data.repository.QuranRepository
import com.example.islam.domain.model.VerseModel
import com.example.islam.quran.QuranAudioPlayer
import com.example.islam.quran.QuranAudioState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SurahReaderUiState(
    val title: String = "",
    val subtitle: String = "",
    val verses: List<VerseModel> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isJuz: Boolean = false
)

@HiltViewModel
class SurahReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: QuranRepository,
    val audioPlayer: QuranAudioPlayer,
    private val prefsDataStore: UserPreferencesDataStore
) : ViewModel() {

    private val surahId: String = savedStateHandle.get<String>("surahId") ?: "67"
    private val surahName: String = savedStateHandle.get<String>("surahName") ?: "Al-Mulk"
    private val isJuz: Boolean = savedStateHandle.get<String>("isJuz")?.toBooleanStrictOrNull() ?: false
    private val startVerse: Int = savedStateHandle.get<String>("startVerse")?.toIntOrNull() ?: 0

    private val _uiState = MutableStateFlow(SurahReaderUiState())
    val uiState: StateFlow<SurahReaderUiState> = _uiState.asStateFlow()

    val audioState: StateFlow<QuranAudioState> = audioPlayer.state

    init {
        loadContent()
        viewModelScope.launch {
            if (isJuz) return@launch
            audioPlayer.state
                .map { it.currentVerseIndex }
                .distinctUntilChanged()
                .collect { index ->
                    val total = audioPlayer.state.value.totalVerses
                    if (total > 0) {
                        val num = surahId.toIntOrNull() ?: return@collect
                        prefsDataStore.updateLastRead(num, surahName, index + 1, total)
                    }
                }
        }
    }

    fun retry() = loadContent()

    private fun loadContent() {
        if (isJuz) loadJuzVerses() else loadSurahVerses()
    }

    private fun loadSurahVerses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                title = "Surah $surahName",
                isLoading = true,
                error = null
            )
            val num = surahId.toIntOrNull() ?: 67
            launch {
                delay(25_000)
                if (_uiState.value.isLoading && _uiState.value.verses.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Yükleme çok uzun sürdü. İnternet bağlantınızı kontrol edip \"Tekrar Dene\" ile yenileyin."
                    )
                }
            }
            repository.getSurahVersesWithTranslation(num)
                .onSuccess { (subtitle, verses) ->
                    if (_uiState.value.isLoading) {
                        _uiState.value = _uiState.value.copy(
                            subtitle = subtitle,
                            verses = verses,
                            isLoading = false,
                            error = null,
                            isJuz = false
                        )
                        if (verses.isNotEmpty()) {
                            audioPlayer.preparePlaylist(num, verses.size, surahName)
                            if (startVerse > 0 && startVerse <= verses.size) {
                                audioPlayer.playVerse((startVerse - 1).coerceIn(0, verses.size - 1))
                            }
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Ayetler yüklenemedi"
                    )
                }
        }
    }

    private fun loadJuzVerses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                title = "Cüz $surahId",
                subtitle = "Juz $surahId",
                isLoading = true,
                error = null,
                isJuz = true
            )
            val juzNum = surahId.toIntOrNull() ?: 1
            launch {
                delay(25_000)
                if (_uiState.value.isLoading && _uiState.value.verses.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Yükleme çok uzun sürdü. İnternet bağlantınızı kontrol edip \"Tekrar Dene\" ile yenileyin."
                    )
                }
            }
            repository.getJuzVerses(juzNum)
                .onSuccess { (sub, verses) ->
                    if (_uiState.value.isLoading) {
                        _uiState.value = _uiState.value.copy(
                            subtitle = sub,
                            verses = verses,
                            isLoading = false,
                            error = null
                        )
                        if (verses.isNotEmpty()) {
                            val verseList = verses.map { (it.surahNumber ?: 1) to it.numberInSurah }
                            audioPlayer.preparePlaylistFromVerseList(verseList, "Cüz $juzNum")
                        }
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Ayetler yüklenemedi"
                    )
                }
        }
    }

    fun playVerse(index: Int) {
        audioPlayer.playVerse(index)
        if (!isJuz && _uiState.value.verses.isNotEmpty()) {
            val num = surahId.toIntOrNull() ?: return
            viewModelScope.launch {
                prefsDataStore.updateLastRead(num, surahName, index + 1, _uiState.value.verses.size)
            }
        }
    }

    fun playPause() {
        audioPlayer.playPause()
    }

    fun nextVerse() {
        audioPlayer.nextVerse()
    }

    fun previousVerse() {
        audioPlayer.previousVerse()
    }

    fun updateAudioPosition() {
        audioPlayer.updatePosition()
    }

    override fun onCleared() {
        // Sayfadan çıkıldığında sesi durdur; geri gelince kullanıcı play ile kaldığı yerden devam edebilir
        audioPlayer.pauseWhenLeavingScreen()
    }
}
