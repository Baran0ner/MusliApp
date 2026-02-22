package com.example.islam.quran

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** EveryAyah.com — Mishary Alafasy 128kbps. Format: 067001.mp3 = surah 67, verse 1 */
private const val AUDIO_BASE = "https://everyayah.com/data/Alafasy_128kbps/"

data class QuranAudioState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentSurahName: String = "",
    val currentVerseIndex: Int = 0,
    val totalVerses: Int = 0,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val error: String? = null
)

@Singleton
class QuranAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var exoPlayer: ExoPlayer? = null
        get() {
            if (field == null) {
                field = ExoPlayer.Builder(context).build().apply {
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            _state.value = _state.value.copy(
                                isLoading = playbackState == Player.STATE_BUFFERING,
                                isPlaying = playbackState == Player.STATE_READY && isPlaying
                            )
                            if (playbackState == Player.STATE_ENDED && verseUrls.isNotEmpty()) {
                                val next = _state.value.currentVerseIndex + 1
                                if (next < verseUrls.size)
                                    Handler(Looper.getMainLooper()).post { playVerse(next) }
                            }
                        }
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _state.value = _state.value.copy(isPlaying = isPlaying)
                        }
                    })
                }
            }
            return field
        }

    private val _state = MutableStateFlow(QuranAudioState())
    val state: StateFlow<QuranAudioState> = _state.asStateFlow()

    private var verseUrls: List<String> = emptyList()
    private var surahName: String = ""

    fun preparePlaylist(surahNumber: Int, verseCount: Int, surahName: String) {
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        this.surahName = surahName
        verseUrls = (1..verseCount).map { verse ->
            "$AUDIO_BASE${surahNumber.toString().padStart(3, '0')}${verse.toString().padStart(3, '0')}.mp3"
        }
        _state.value = QuranAudioState(
            currentSurahName = surahName,
            currentVerseIndex = 0,
            totalVerses = verseUrls.size,
            positionMs = 0L,
            durationMs = 0L
        )
    }

    fun playVerse(index: Int) {
        if (index < 0 || index >= verseUrls.size) return
        val url = verseUrls[index]
        _state.value = _state.value.copy(currentVerseIndex = index, isLoading = true, error = null)
        exoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
            play()
        }
    }

    fun playPause() {
        if (verseUrls.isEmpty()) return
        exoPlayer?.let { p ->
            if (p.isPlaying) {
                p.pause()
            } else {
                if (p.playbackState == Player.STATE_IDLE || p.playbackState == Player.STATE_ENDED)
                    playVerse(_state.value.currentVerseIndex)
                else
                    p.play()
            }
        } ?: run { playVerse(_state.value.currentVerseIndex) }
    }

    fun nextVerse(): Boolean {
        val idx = _state.value.currentVerseIndex
        if (idx + 1 >= verseUrls.size) return false
        playVerse(idx + 1)
        return true
    }

    fun previousVerse(): Boolean {
        val idx = _state.value.currentVerseIndex
        if (idx <= 0) {
            exoPlayer?.seekTo(0)
            return false
        }
        playVerse(idx - 1)
        return true
    }

    fun seekToVerse(index: Int) {
        if (index in verseUrls.indices) playVerse(index)
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        _state.value = QuranAudioState()
    }

    fun updatePosition() {
        exoPlayer?.let { p ->
            _state.value = _state.value.copy(
                positionMs = p.currentPosition,
                durationMs = p.duration.coerceAtLeast(0)
            )
        }
    }

    /** Sayfadan çıkıldığında sesi duraklatır; geri gelince kullanıcı play ile devam edebilir. */
    fun pauseWhenLeavingScreen() {
        exoPlayer?.pause()
    }
}
