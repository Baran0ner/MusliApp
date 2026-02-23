package com.example.islam.presentation.quran

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.islam.core.i18n.LocalStrings
import com.example.islam.core.navigation.Screen
import com.example.islam.domain.model.JuzListModel
import com.example.islam.domain.model.SurahListModel
import com.example.islam.ui.theme.AmiriFamily

// ─── Referans HTML renkleri ───────────────────────────────────────────────────
private val RefPrimary = Color(0xFFD4AF37)
private val RefBackgroundDark = Color(0xFF0B2419)
private val RefSurfaceCard = Color(0xFF133326)
private val RefSurfaceCard30 = Color(0x4D133326)
private val RefSurfaceCard40 = Color(0x66133326)
private val RefBorderPrimary20 = Color(0x33D4AF37)
private val RefBorderPrimary30 = Color(0x4DD4AF37)
private val RefBorderWhite5 = Color(0x0DFFFFFF)
private val RefTextWhite = Color.White
private val RefTextGray400 = Color(0xFF9CA3AF)
private val RefTextGray500 = Color(0xFF6B7280)
private val RefRed500 = Color(0xFFEF4444)

@Composable
fun QuranScreen(
    navController: NavController,
    viewModel: QuranViewModel = hiltViewModel()
) {
    val strings = LocalStrings.current
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var searchExpanded by remember { mutableStateOf(false) }
    var displayedCount by remember { mutableStateOf(10) }
    val searchFocusRequester = remember { FocusRequester() }
    val uiState by viewModel.uiState.collectAsState()
    val lastRead by viewModel.lastRead.collectAsState()
    val bookmarkedSurahIds by viewModel.bookmarkedSurahIds.collectAsState()
    val filteredSurahs = remember(uiState.surahs, searchQuery) {
        if (searchQuery.isBlank()) uiState.surahs
        else {
            val q = searchQuery.lowercase().trim()
            val trDisplay = com.example.islam.data.repository.StaticSurahData
            uiState.surahs.filter { surah ->
                surah.englishName.lowercase().contains(q) ||
                    surah.englishNameTranslation.lowercase().contains(q) ||
                    surah.arabicName.contains(searchQuery.trim()) ||
                    (surah.turkishDisplayName?.lowercase()?.contains(q) == true) ||
                    (trDisplay.getTurkishDisplayName(surah.number)?.lowercase()?.contains(q) == true) ||
                    (surah.turkishNameTranslation?.lowercase()?.contains(q) == true) ||
                    (trDisplay.getTurkishTranslation(surah.number)?.lowercase()?.contains(q) == true)
            }
        }
    }
    val displayedSurahs = remember(filteredSurahs, searchQuery, displayedCount) {
        if (searchQuery.isNotBlank()) filteredSurahs else filteredSurahs.take(displayedCount)
    }
    val canLoadMore = searchQuery.isBlank() && filteredSurahs.size > displayedCount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RefBackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading && uiState.surahs.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = RefPrimary)
                            Spacer(Modifier.height(16.dp))
                            Text("Sureler yükleniyor…", color = RefTextGray400, fontSize = 14.sp)
                        }
                    }
                }
                uiState.error != null && uiState.surahs.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(uiState.error!!, color = RefTextGray400, fontSize = 14.sp)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadSurahs(refresh = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = RefPrimary),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                            ) { Text(strings.retryButton, color = RefBackgroundDark) }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding()
                            .padding(horizontal = 24.dp)
                            .padding(top = 20.dp, bottom = 96.dp)
                    ) {
                        // Greeting — sayfa doğrudan buradan başlıyor
                        Text(
                            "Esselamü Aleyküm",
                            fontSize = 14.sp,
                            color = RefTextGray400,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        // İsim ile aynı satırda sağda arama
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Tanvir Ahassan",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = RefTextWhite
                            )
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(RefSurfaceCard)
                                    .border(1.dp, RefBorderPrimary20, CircleShape)
                                    .clickable { searchExpanded = !searchExpanded },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Search,
                                    contentDescription = "Ara",
                                    tint = RefPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (searchExpanded) {
                            Spacer(Modifier.height(12.dp))
                            LaunchedEffect(Unit) {
                                searchFocusRequester.requestFocus()
                            }
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(searchFocusRequester),
                                placeholder = { Text("Sure veya ayet ara…", color = RefTextGray500, fontSize = 14.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RefPrimary,
                                    unfocusedBorderColor = RefBorderPrimary20,
                                    cursorColor = RefPrimary,
                                    focusedTextColor = RefTextWhite,
                                    unfocusedTextColor = RefTextWhite
                                )
                            )
                        }
                        Spacer(Modifier.height(24.dp))

                        // Last Read card
                        val bannerSurahId = lastRead?.surahId ?: 1
                        val bannerSurahName = lastRead?.surahName ?: "Al-Faatiha"
                        val bannerDisplayName = uiState.surahs.find { it.number == bannerSurahId }?.turkishDisplayName
                            ?: com.example.islam.data.repository.StaticSurahData.getTurkishDisplayName(bannerSurahId)
                            ?: bannerSurahName
                        val bannerAyahNo = lastRead?.verseNumber ?: 1
                        val bannerProgress = lastRead?.let {
                            it.verseNumber.toFloat() / it.totalVerses.coerceAtLeast(1)
                        } ?: 0.25f
                        LastReadCard(
                            surahName = bannerDisplayName,
                            ayahNo = bannerAyahNo,
                            progressFraction = bannerProgress,
                            onCardClick = {
                                navController.navigate(Screen.SurahReader.route(bannerSurahId, bannerSurahName, false, bannerAyahNo))
                            },
                            onPlayClick = {
                                navController.navigate(Screen.SurahReader.route(bannerSurahId, bannerSurahName, false, bannerAyahNo))
                            }
                        )
                        Spacer(Modifier.height(32.dp))

                        // Tabs: Surah | Juz | Bilinen
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(RefSurfaceCard30)
                                .border(1.dp, RefBorderWhite5, RoundedCornerShape(20.dp))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            listOf("Sure", "Cüz", "Bilinen").forEachIndexed { index, label ->
                                val selected = selectedTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .then(
                                            if (selected) Modifier.background(RefPrimary)
                                            else Modifier
                                        )
                                        .clickable { selectedTab = index }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (selected) RefBackgroundDark else RefTextGray400
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))

                        when (selectedTab) {
                            0 -> {
                                displayedSurahs.forEach { surah ->
                                    SurahRow(
                                        surah = surah.copy(isBookmarked = surah.number in bookmarkedSurahIds),
                                        onClick = { navController.navigate(Screen.SurahReader.route(surah.number, surah.name, false, 1)) },
                                        onBookmarkClick = { viewModel.toggleBookmark(surah.number) },
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                }
                                if (canLoadMore) {
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { displayedCount = (displayedCount + 10).coerceAtMost(filteredSurahs.size) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RefPrimary),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, RefBorderPrimary20)
                                    ) {
                                        Text("Daha fazla (${displayedSurahs.size}/${filteredSurahs.size})")
                                    }
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                            1 -> uiState.juzs.forEach { juz ->
                                JuzRow(
                                    juz = juz,
                                    onClick = { navController.navigate(Screen.SurahReader.route(juz.number, juz.displayName, true, 0)) },
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }
                            2 -> {
                                val bilinen = filteredSurahs.filter { it.number in bookmarkedSurahIds }
                                if (bilinen.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Yer imi eklemek için surelerin yanındaki işarete basın.",
                                            color = RefTextGray400,
                                            fontSize = 14.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    bilinen.sortedBy { it.number }.forEach { surah ->
                                        SurahRow(
                                            surah = surah.copy(isBookmarked = true),
                                            onClick = { navController.navigate(Screen.SurahReader.route(surah.number, surah.name, false, 1)) },
                                            onBookmarkClick = { viewModel.toggleBookmark(surah.number) },
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LastReadCard(
    surahName: String,
    ayahNo: Int,
    progressFraction: Float,
    onCardClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Glow
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(4.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(RefPrimary.copy(alpha = 0.2f), RefSurfaceCard.copy(alpha = 0f))
                    )
                )
                .graphicsLayer { alpha = 0.4f }
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            // Dekoratif kitap ikonu (sağ alt, çok soluk)
            Icon(
                imageVector = Icons.Outlined.MenuBook,
                contentDescription = null,
                tint = RefTextWhite.copy(alpha = 0.05f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(120.dp)
                    .offset(x = 24.dp, y = 40.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(RefSurfaceCard)
                    .border(1.dp, RefBorderPrimary30, RoundedCornerShape(32.dp))
                    .clickable(onClick = onCardClick)
                    .padding(24.dp)
            ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.MenuBook,
                    contentDescription = null,
                    tint = RefTextGray400,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Son Okunan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = RefTextGray400
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                surahName,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                color = RefTextWhite
            )
            Text(
                "Ayet No: $ayahNo",
                fontSize = 14.sp,
                color = RefTextGray400,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(RefBackgroundDark.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                            .clip(RoundedCornerShape(3.dp))
                            .shadow(4.dp, RoundedCornerShape(3.dp), spotColor = RefPrimary.copy(alpha = 0.8f))
                            .background(RefPrimary)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(RefPrimary)
                        .clickable(onClick = onPlayClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.PlayArrow,
                        contentDescription = "Oynat",
                        tint = RefBackgroundDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            }
        }
    }
}

/** Elmas şeklinde sure numarası (elmas 45°, sayı ekranda dik) */
@Composable
private fun SurahNumberDiamond(number: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(36.dp),
        contentAlignment = Alignment.Center
    ) {
        // Elmas arka plan: Canvas ile çizilmiş 4 köşe elmas (döndürme yok, sayı etkilenmez)
        Canvas(modifier = Modifier.size(36.dp)) {
            val s = size.minDimension
            val cx = s / 2f
            val half = s / 2f - 2
            val path = Path().apply {
                moveTo(cx, cx - half)
                lineTo(cx + half, cx)
                lineTo(cx, cx + half)
                lineTo(cx - half, cx)
                close()
            }
            drawPath(path, RefPrimary.copy(alpha = 0.1f))
            drawPath(path, RefPrimary.copy(alpha = 0.4f), style = Stroke(1.5.dp.toPx()))
        }
        // Sayı: hiçbir döndürme yok, ekranda dik
        Text(
            text = number.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = RefPrimary
        )
    }
}

@Composable
private fun SurahRow(
    surah: SurahListModel,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(RefSurfaceCard40)
            .border(1.dp, RefBorderWhite5, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SurahNumberDiamond(number = surah.number)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = surah.turkishDisplayName ?: com.example.islam.data.repository.StaticSurahData.getTurkishDisplayName(surah.number) ?: surah.englishName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = RefTextWhite
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = surah.turkishNameTranslation
                        ?: com.example.islam.data.repository.StaticSurahData.getTurkishTranslation(surah.number)
                        ?: surah.englishNameTranslation,
                    fontSize = 12.sp,
                    color = RefTextGray400
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(RefTextGray500)
                )
                Text(
                    text = "${surah.numberOfAyahs} Ayet",
                    fontSize = 12.sp,
                    color = RefTextGray400
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            if (surah.arabicName.isNotBlank()) {
                Text(
                    text = surah.arabicName,
                    fontSize = 20.sp,
                    fontFamily = AmiriFamily,
                    color = RefPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            IconButton(
                onClick = onBookmarkClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (surah.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkAdd,
                    contentDescription = if (surah.isBookmarked) "Yer iminden çıkar" else "Yer imi ekle",
                    tint = if (surah.isBookmarked) RefPrimary else RefTextGray500,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun JuzRow(
    juz: JuzListModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(RefSurfaceCard40)
            .border(1.dp, RefBorderWhite5, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SurahNumberDiamond(number = juz.number)
        Text(
            text = juz.displayName,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = RefTextWhite,
            modifier = Modifier.weight(1f)
        )
    }
}
