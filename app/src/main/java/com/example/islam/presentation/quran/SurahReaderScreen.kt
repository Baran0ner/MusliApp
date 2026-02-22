package com.example.islam.presentation.quran

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.islam.domain.model.VerseModel
import com.example.islam.ui.theme.AmiriFamily

// ─────────────────────────────────────────────────────────────────────────────
// HTML/Tailwind renkleri
// ─────────────────────────────────────────────────────────────────────────────
private val PrimaryGreen    = Color(0xFF0B7944)
private val PrimaryDark     = Color(0xFF064E2B)
private val AccentYellow    = Color(0xFFFACC15)
private val White           = Color(0xFFFFFFFF)
private val Slate500        = Color(0xFF64748B)
private val Slate800        = Color(0xFF1E293B)
private val Gray100         = Color(0xFFF3F4F6)

// ─────────────────────────────────────────────────────────────────────────────
// SurahReaderScreen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SurahReaderScreen(
    navController: NavController,
    surahId: String,
    surahName: String,
    isJuz: Boolean = false,
    startVerse: Int = 0,
    viewModel: SurahReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val audioState by viewModel.audioState.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            viewModel.updateAudioPosition()
            delay(500)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ReaderTopBar(
                title = uiState.title.ifEmpty { if (isJuz) "Cüz $surahName" else "Surah $surahName" },
                subtitle = uiState.subtitle,
                onBackClick = { navController.popBackStack() },
                onMoreClick = { }
            )

            when {
                uiState.isLoading && uiState.verses.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                uiState.error != null && uiState.verses.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(uiState.error!!, color = Slate500)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.retry() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) { Text("Tekrar Dene") }
                        }
                    }
                }
                else -> {
                    if (!isJuz) BismillahSection()
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 120.dp)
                    ) {
                        uiState.verses.forEachIndexed { index, verse ->
                            VerseBlock(
                                verse = verse,
                                isCurrentVerse = audioState.currentVerseIndex == index,
                                onPlayClick = { viewModel.playVerse(index) }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.verses.isNotEmpty() && !uiState.isJuz) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                val progress = if (audioState.durationMs > 0)
                    (audioState.positionMs.toFloat() / audioState.durationMs).coerceIn(0f, 1f)
                else 0f
                FloatingAudioPlayer(
                    reciterName = audioState.currentSurahName.ifEmpty { surahName },
                    currentTime = formatTime(audioState.positionMs),
                    progress = progress,
                    isPlaying = audioState.isPlaying,
                    isLoading = audioState.isLoading,
                    onPlayPause = { viewModel.playPause() },
                    onPrevious = { viewModel.previousVerse() },
                    onNext = { viewModel.nextVerse() }
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val s = (ms / 1000).toInt()
    val m = s / 60
    val sec = s % 60
    return "%d:%02d".format(m, sec)
}

// ─────────────────────────────────────────────────────────────────────────────
// Top bar — bg-primary, back | title+subtitle | more_vert
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ReaderTopBar(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Surface(
        color = PrimaryGreen,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Geri",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = White.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            }
            IconButton(onClick = onMoreClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "Daha fazla",
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bismillah — bg-primary-dark/30, border-t white/10, Arapça metin
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BismillahSection() {
    Surface(
        color = PrimaryDark.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
            fontFamily = AmiriFamily,
            fontSize = 20.sp,
            color = White.copy(alpha = 0.9f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tek ayet bloğu — rozet, Arapça (sağa hizalı), transliteration, çeviri
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun VerseBlock(
    verse: VerseModel,
    isCurrentVerse: Boolean = false,
    onPlayClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick)
            .border(1.dp, Gray100.copy(alpha = 0.5f))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            AyahBadge(number = verse.numberInSurah)
            Spacer(Modifier.width(16.dp))
            Text(
                text = verse.arabic,
                fontFamily = AmiriFamily,
                fontSize = 28.sp,
                lineHeight = 56.sp,
                color = Slate800,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.padding(start = 56.dp)) {
            if (verse.transliteration.isNotBlank()) {
                Text(
                    text = verse.transliteration,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryGreen,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                text = verse.translation,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Slate500,
                lineHeight = 22.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Ayah rozeti — yıldız (opacity 0.15) + daire çerçeve, numara
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AyahBadge(number: Int) {
    Box(
        modifier = Modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(36.dp)) {
            val cx = size.minDimension / 2f
            val cy = size.minDimension / 2f
            // Rub el Hizb yıldızı (8 uçlu) — opacity 0.15
            val starPath = Path().apply {
                val outerR = size.minDimension * 0.4f
                val innerR = outerR * 0.4f
                for (i in 0 until 8) {
                    val angleOut = (i * 45 - 90) * Math.PI / 180
                    val xOut = cx + (outerR * kotlin.math.cos(angleOut)).toFloat()
                    val yOut = cy + (outerR * kotlin.math.sin(angleOut)).toFloat()
                    val angleIn = ((i + 0.5) * 45 - 90) * Math.PI / 180
                    val xIn = cx + (innerR * kotlin.math.cos(angleIn)).toFloat()
                    val yIn = cy + (innerR * kotlin.math.sin(angleIn)).toFloat()
                    if (i == 0) moveTo(xOut, yOut) else lineTo(xOut, yOut)
                    lineTo(xIn, yIn)
                }
                close()
            }
            drawPath(starPath, color = PrimaryGreen.copy(alpha = 0.15f))
            // Daire çerçeve
            drawCircle(
                color = PrimaryGreen,
                radius = size.minDimension / 2f - 2,
                style = Stroke(width = 2f)
            )
        }
        Text(
            text = "$number",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Floating Audio Player — rounded-full, reciter, progress, prev/play/next
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FloatingAudioPlayer(
    reciterName: String,
    currentTime: String,
    progress: Float,
    isPlaying: Boolean,
    isLoading: Boolean = false,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 400.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(percent = 50),
        color = PrimaryGreen.copy(alpha = 0.98f),
        shadowElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Reciter avatar — Rub el Hizb ikonu
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(PrimaryDark.copy(alpha = 0.5f))
                    .border(1.dp, White.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(24.dp)) {
                    val cx = size.minDimension / 2f
                    val cy = size.minDimension / 2f
                    val r = size.minDimension * 0.4f
                    val path = Path().apply {
                        for (i in 0 until 8) {
                            val angle = (i * 45 - 90) * Math.PI / 180
                            val x = cx + (r * kotlin.math.cos(angle)).toFloat()
                            val y = cy + (r * kotlin.math.sin(angle)).toFloat()
                            if (i == 0) moveTo(x, y) else lineTo(x, y)
                        }
                        close()
                    }
                    drawPath(path, color = AccentYellow)
                }
            }

            // Bilgi + progress
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reciterName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = currentTime,
                        fontSize = 10.sp,
                        color = White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(2.dp))
                            .background(AccentYellow)
                    )
                }
            }

            // Kontroller
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SkipPrevious,
                        contentDescription = "Önceki",
                        tint = White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentYellow)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = PrimaryDark,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                            tint = PrimaryDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SkipNext,
                        contentDescription = "Sonraki",
                        tint = White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
