package com.example.islam.presentation.dhikr

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.islam.core.i18n.LocalStrings
import com.example.islam.domain.model.Dhikr
import com.example.islam.domain.model.DhikrDay
import com.example.islam.domain.model.DhikrRecord
import java.text.SimpleDateFormat
import java.util.*

// ─── Renkler ──────────────────────────────────────────────────────────────────
private val BgDeep     = Color(0xFF091811)
private val BgMid      = Color(0xFF0D2018)
private val Gold       = Color(0xFFD4AF37)
private val GoldDim    = Color(0x66D4AF37)
private val GoldFaint  = Color(0x22D4AF37)
private val TextWhite  = Color.White
private val TextMuted  = Color(0x99FFFFFF)
private val ChipBg     = Color(0xFF132B1A)
private val ChipActive = Color(0xFF1E4A2E)

// ─── Ekran ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrScreen(viewModel: DhikrViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val dhikr = state.selectedDhikr
    val strings = LocalStrings.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BgMid, BgDeep, BgDeep)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Başlık
            DhikrHeader()

            // Zikir seçici
            if (state.dhikrList.isNotEmpty()) {
                DhikrSelector(
                    list     = state.dhikrList,
                    selected = dhikr,
                    onSelect = viewModel::selectDhikr
                )
            }

            Spacer(Modifier.height(24.dp))

            // Arapça metin + anlam
            if (dhikr != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ChipBg)
                        .border(1.dp, GoldFaint, RoundedCornerShape(8.dp))
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = dhikr.arabicText,
                            fontSize   = 36.sp,
                            color      = Gold,
                            textAlign  = TextAlign.Center,
                            lineHeight = 52.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text       = dhikr.meaning,
                            fontSize   = 13.sp,
                            color      = TextMuted,
                            textAlign  = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Devir sayısı
            if (state.cycleCount > 0) {
                Text(
                    text     = strings.dhikrCycleCompletedFormat.format(state.cycleCount),
                    fontSize = 13.sp,
                    color    = Gold.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
            }

            // Sayaç + büyük buton
            TasbihCounter(
                count       = dhikr?.count ?: 0,
                target      = dhikr?.targetCount ?: 33,
                celebrating = state.isCelebrating,
                onClick     = { if (dhikr != null) viewModel.increment() }
            )

            Spacer(Modifier.height(24.dp))

            // Sıfırla + Kaydet butonları
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                OutlinedButton(
                    onClick = viewModel::reset,
                    border  = androidx.compose.foundation.BorderStroke(1.dp, GoldDim),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                    shape   = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Outlined.Refresh,
                        contentDescription = strings.reset,
                        modifier           = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(strings.reset, fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = viewModel::saveCurrent,
                    border  = androidx.compose.foundation.BorderStroke(1.dp, GoldDim),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                    shape   = RoundedCornerShape(8.dp)
                ) {
                    Text(strings.saveDhikr, fontSize = 14.sp)
                }
            }

            if (state.weeklyDays.isNotEmpty()) {
                DhikrWeeklyCard(
                    days = state.weeklyDays,
                    onDayClick = viewModel::selectDay
                )
                Spacer(Modifier.height(32.dp))
            }
        }

        if (state.showDaySheet && state.selectedDay != null) {
            androidx.compose.material3.ModalBottomSheet(
                onDismissRequest = viewModel::dismissDaySheet,
                containerColor = ChipBg,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 8.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Gold.copy(alpha = 0.4f))
                    )
                }
            ) {
                DhikrDaySheetContent(
                    day = state.selectedDay!!,
                    records = state.dayDetailRecords,
                    onDismiss = viewModel::dismissDaySheet
                )
            }
        }
    }
}

// ─── Başlık ───────────────────────────────────────────────────────────────────
@Composable
private fun DhikrHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(1.dp)
                .background(Gold.copy(alpha = 0.5f))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text       = "ذِكْر",
            fontSize   = 28.sp,
            color      = Gold,
            letterSpacing = 2.sp
        )
        Text(
            text       = "TESPİH",
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
            color      = TextWhite,
            letterSpacing = 3.sp
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(1.dp)
                .background(Gold.copy(alpha = 0.5f))
        )
    }
}

// ─── Zikir Seçici ─────────────────────────────────────────────────────────────
@Composable
private fun DhikrSelector(
    list     : List<Dhikr>,
    selected : Dhikr?,
    onSelect : (Dhikr) -> Unit
) {
    LazyRow(
        contentPadding    = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier          = Modifier.fillMaxWidth()
    ) {
        items(list) { dhikr ->
            val isSelected = dhikr.id == selected?.id
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp)) // ADM button radius
                    .background(if (isSelected) ChipActive else ChipBg)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Gold.copy(alpha = 0.6f) else GoldFaint,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = ripple(color = Gold)
                    ) { onSelect(dhikr) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text       = dhikr.name,
                    fontSize   = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color      = if (isSelected) Gold else TextMuted
                )
            }
        }
    }
}

// ─── Tespih Sayaç Butonu ──────────────────────────────────────────────────────
@Composable
private fun TasbihCounter(
    count       : Int,
    target      : Int,
    celebrating : Boolean,
    onClick     : () -> Unit
) {
    val progress = if (target > 0) count.toFloat() / target else 0f
    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(durationMillis = 350),
        label         = "progress"
    )
    val scale by animateFloatAsState(
        targetValue   = if (celebrating) 1.08f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label         = "scale"
    )
    val ringColor by animateColorAsState(
        targetValue = if (celebrating) Color(0xFFFFD700) else Gold,
        label       = "ring"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Sayı
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text       = "$count",
                fontSize   = 64.sp,
                fontWeight = FontWeight.Bold,
                color      = TextWhite,
                lineHeight = 64.sp
            )
            Text(
                text     = " / $target",
                fontSize = 20.sp,
                color    = TextMuted,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        // İlerleme yayı + buton
        Box(
            modifier         = Modifier.scale(scale),
            contentAlignment = Alignment.Center
        ) {
            // Canvas: arka track + ilerleme yayı
            Canvas(modifier = Modifier.size(178.dp)) {
                val strokePx = 5.dp.toPx()
                val radius   = size.minDimension / 2f - strokePx / 2f
                val topLeft  = Offset(center.x - radius, center.y - radius)
                val arcSize  = Size(radius * 2f, radius * 2f)

                // Track halkası (soluk)
                drawArc(
                    color      = Gold.copy(alpha = 0.18f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter  = false,
                    topLeft    = topLeft,
                    size       = arcSize,
                    style      = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // İlerleme yayı
                if (animatedProgress > 0f) {
                    drawArc(
                        color      = ringColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter  = false,
                        topLeft    = topLeft,
                        size       = arcSize,
                        style      = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }

            // Tıklanabilir büyük daire
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2D6B40),
                                Color(0xFF183D25)
                            )
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = ripple(bounded = true, color = Gold, radius = 80.dp)
                    ) { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = if (celebrating) "✓" else "الله",
                    fontSize  = if (celebrating) 52.sp else 36.sp,
                    color     = if (celebrating) Color(0xFFFFD700) else Gold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─── Haftalık tespih durumu ───────────────────────────────────────────────────
@Composable
private fun DhikrWeeklyCard(
    days: List<DhikrDay>,
    onDayClick: (DhikrDay) -> Unit
) {
    val strings = LocalStrings.current
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val daysWithActivity = days.count { it.records.isNotEmpty() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(ChipBg)
            .border(1.dp, GoldFaint, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.dhikrWeeklyTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
            Text(
                text = strings.dhikrWeeklySummaryFormat.format(daysWithActivity),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Gold
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                DhikrDayItem(
                    day = day,
                    isToday = day.date == todayStr,
                    onClick = { onDayClick(day) }
                )
            }
        }
    }
}

@Composable
private fun DhikrDayItem(
    day: DhikrDay,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val total = day.totalCount
    val hasActivity = day.records.isNotEmpty()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (hasActivity) Gold.copy(alpha = 0.2f) else BgDeep),
                contentAlignment = Alignment.Center
        ) {
            if (hasActivity) {
                Text(
                    text = "📿",
                    fontSize = 14.sp
                )
            }
        }
        Text(
            text = day.shortName,
            fontSize = 10.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            color = if (isToday) Gold else TextMuted
        )
        if (hasActivity) {
            Text(
                text = "${total}",
                fontSize = 9.sp,
                color = Gold.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun DhikrDaySheetContent(
    day: DhikrDay,
    records: List<DhikrRecord>,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val displayDate = remember(day.date) {
        try {
            val parsed = SimpleDateFormat("yyyy-MM-dd", Locale("tr")).parse(day.date)
            SimpleDateFormat("d MMMM yyyy, EEEE", Locale("tr")).format(parsed!!)
        } catch (e: Exception) {
            day.date
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = displayDate,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Gold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (records.isEmpty()) {
            Text(
                text = strings.noDhikrToday,
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            val aggregated = remember(records) {
                records.groupBy { it.dhikrName }.map { (name, list) ->
                    name to list.sumOf { it.count }
                }
            }
            aggregated.forEach { (name, total) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextWhite
                    )
                    Text(
                        text = "$total adet",
                        fontSize = 14.sp,
                        color = Gold
                    )
                }
            }
        }
    }
}
