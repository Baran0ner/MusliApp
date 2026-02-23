package com.example.islam.presentation.prayer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.islam.R
import androidx.navigation.NavController
import com.example.islam.core.i18n.LocalStrings
import com.example.islam.core.navigation.Screen
import com.example.islam.domain.model.Prayer
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.model.PrayerType
import com.example.islam.domain.model.WeekDay
import com.example.islam.domain.model.timeFor
import com.example.islam.core.util.DateUtil.cleanTime
import java.text.SimpleDateFormat
import java.util.*

// ─── Referans HTML renkleri ───────────────────────────────────────────────────
private val RefPrimary = Color(0xFFd4af35)
private val RefDeepGreen = Color(0xFF0b261d)
private val RefCardGreen = Color(0xFF113025)
private val RefCardBorder = Color(0xFF1f4a3a)
private val RefTextGold = Color(0xFFe6c768)
private val RefTextSecondary = Color(0xFFa3b8b0)
private val RefWhite = Color.White
private val RefBorderPrimary30 = Color(0x4Dd4af35)
private val RefBorderPrimary40 = Color(0x66d4af35)
private val RefBorderPrimary60 = Color(0x99d4af35)
private val RefBorderWhite5 = Color(0x0DFFFFFF)
private val RefBorderWhite10 = Color(0x1AFFFFFF)

private val TRACKABLE_PRAYERS = listOf(
    Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerScreen(
    navController: NavController,
    viewModel: PrayerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RefDeepGreen)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Sticky header ─────────────────────────────────────────────────
            PrayerHeader(
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onCalendarClick = { }
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = RefPrimary)
                    }
                }
                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.error!!,
                                color = Color(0xFFE57373),
                                modifier = Modifier.padding(16.dp)
                            )
                            TextButton(onClick = viewModel::refresh) {
                                Text(strings.refresh, color = RefPrimary)
                            }
                        }
                    }
                }
                state.prayerTime != null -> {
                    val pt = state.prayerTime!!
                    val currentPrayer = state.currentPrayer
                    val displayPrayer = currentPrayer ?: Prayer.FAJR
                    val displayTime = pt.timeFor(displayPrayer).cleanTime()
                    val countdownStr = "Sonraki vakte ${state.countdownText}"

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 12.dp, bottom = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = displayPrayer.turkishName,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                    color = RefWhite
                                )
                                Text(
                                    text = displayTime,
                                    fontSize = 64.sp,
                                    lineHeight = 64.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                    color = RefWhite
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(RefCardGreen.copy(alpha = 0.5f))
                                        .border(1.dp, RefBorderPrimary30, RoundedCornerShape(50.dp))
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = RefTextGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = countdownStr,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = RefTextGold
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(vertical = 8.dp)
                                    .border(1.dp, RefBorderWhite10, RoundedCornerShape(0.dp)),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "İMSAK",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = RefTextSecondary
                                    )
                                    Text(
                                        text = pt.imsak.cleanTime(),
                                        fontSize = 18.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                        color = RefWhite
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(32.dp)
                                        .background(RefBorderWhite10)
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "GÜNEŞ",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = RefTextSecondary
                                    )
                                    Text(
                                        text = pt.sunrise.cleanTime(),
                                        fontSize = 18.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                        color = RefWhite
                                    )
                                }
                            }
                        }

                        items(TRACKABLE_PRAYERS) { prayer ->
                            PrayerTrackerRow(
                                prayer = prayer,
                                time = pt.timeFor(prayer).cleanTime(),
                                isCurrent = prayer == currentPrayer,
                                isCompleted = prayer.name in state.completedPrayers,
                                strings = strings,
                                onToggle = { viewModel.togglePrayerCompleted(prayer) }
                            )
                        }

                        if (state.weeklyHistory.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(8.dp))
                                WeeklyStatusCard(
                                    days = state.weeklyHistory,
                                    onDayClick = viewModel::selectDay
                                )
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }

        if (state.showDaySheet && state.selectedDay != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = viewModel::dismissSheet,
                sheetState = sheetState,
                containerColor = RefCardGreen,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 8.dp)
                            .size(width = 36.dp, height = 4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(RefPrimary.copy(alpha = 0.4f))
                    )
                }
            ) {
                DayEditSheet(
                    day = state.selectedDay!!,
                    onToggle = { type -> viewModel.toggleHistoryPrayer(state.selectedDay!!.date, type) }
                )
            }
        }
    }
}

@Composable
private fun PrayerHeader(
    onSettingsClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    Surface(
        color = RefDeepGreen.copy(alpha = 0.95f),
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            HorizontalDivider(color = RefBorderWhite5, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon_ayarlar),
                        contentDescription = "Ayarlar",
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "Bugünkü Namazlar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    color = RefWhite
                )
                IconButton(
                    onClick = onCalendarClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = "Takvim",
                        tint = RefTextSecondary
                    )
                }
            }
            HorizontalDivider(color = RefBorderWhite5, thickness = 1.dp)
        }
    }
}

@Composable
private fun PrayerTrackerRow(
    prayer: Prayer,
    time: String,
    isCurrent: Boolean,
    isCompleted: Boolean,
    strings: com.example.islam.core.i18n.AppStrings,
    onToggle: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isCurrent) RefBorderPrimary60 else RefCardBorder,
        animationSpec = tween(400),
        label = "border"
    )
    val statusText = when {
        isCurrent -> "Vakit Giriyor"
        isCompleted -> strings.completed
        else -> "Kılınmadı"
    }
    val nameColor = if (isCurrent) RefPrimary else RefWhite
    val statusColor = if (isCurrent) RefPrimary.copy(alpha = 0.8f) else RefTextSecondary
    val timeColor = if (isCurrent) RefPrimary else RefWhite

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .then(
                if (isCurrent) Modifier.shadow(
                    8.dp,
                    RoundedCornerShape(20.dp),
                    spotColor = RefPrimary.copy(alpha = 0.15f)
                ) else Modifier
            )
            .clip(RoundedCornerShape(20.dp))
            .background(RefCardGreen)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onToggle() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Column {
                Text(
                    text = strings.prayerName(prayer),
                    fontSize = 16.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    color = nameColor
                )
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = statusColor
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = time,
                fontSize = 18.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                color = timeColor
            )
            PrayerCheckbox(
                checked = isCompleted,
                borderColor = if (isCurrent) RefPrimary else RefTextSecondary.copy(alpha = 0.5f),
                onClick = onToggle
            )
        }
    }
}

@Composable
private fun PrayerCheckbox(
    checked: Boolean,
    borderColor: Color,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (checked) RefPrimary else Color.Transparent,
        animationSpec = tween(250),
        label = "cb_bg"
    )
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = RefDeepGreen,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun WeeklyStatusCard(
    days: List<WeekDay>,
    onDayClick: (WeekDay) -> Unit
) {
    val completedDays = days.count { it.history.prayedCount >= 5 }
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(RefCardGreen)
            .border(1.dp, RefCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HAFTALIK DURUM",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = RefTextSecondary
            )
            Text(
                text = "$completedDays/7 Gün",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RefPrimary
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                DayRingItem(
                    day = day,
                    isToday = day.date == todayStr,
                    onClick = { onDayClick(day) }
                )
            }
        }
    }
}

@Composable
private fun DayRingItem(
    day: WeekDay,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val progress = (day.history.prayedCount / 5f).coerceIn(0f, 1f)
    val isCompleted = day.history.prayedCount >= 5

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            RingProgress(progress = progress)
            
            if (isCompleted) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = true,
                    enter = androidx.compose.animation.fadeIn(animationSpec = tween(500)) + 
                            androidx.compose.animation.scaleIn(
                                initialScale = 0.5f, 
                                animationSpec = androidx.compose.animation.core.spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                )
                            )
                ) {
                    Text(
                        text = "🤲🏻",
                        fontSize = 14.sp
                    )
                }
            }
        }
        Text(
            text = day.shortName,
            fontSize = 10.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            color = if (isToday) RefPrimary else RefTextSecondary
        )
    }
}

@Composable
private fun RingProgress(progress: Float) {
    Canvas(modifier = Modifier.size(32.dp)) {
        val strokeWidth = 2.dp.toPx()
        val radius = (size.minDimension / 2) - strokeWidth / 2
        val center = Offset(size.width / 2, size.height / 2)
        drawCircle(
            color = RefWhite.copy(alpha = 0.1f),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = RefPrimary,
            startAngle = -90f,
            sweepAngle = 360f * progress,
            useCenter = false,
            topLeft = center - Offset(radius, radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun DayEditSheet(day: WeekDay, onToggle: (PrayerType) -> Unit) {
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
            color = RefPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        PrayerType.entries.forEach { prayerType ->
            val isChecked = day.history.isTracked(prayerType)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isChecked) RefCardGreen else Color.Transparent)
                    .clickable { onToggle(prayerType) }
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = prayerType.displayName,
                        fontSize = 14.sp,
                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                        color = if (isChecked) RefPrimary else RefWhite
                    )
                    Text(
                        text = prayerType.arabicName,
                        fontSize = 11.sp,
                        color = RefTextSecondary
                    )
                }
                val checkBg by animateColorAsState(
                    targetValue = if (isChecked) RefPrimary else Color.Transparent,
                    animationSpec = tween(250),
                    label = "sheet_cb"
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(checkBg)
                        .border(
                            width = 1.5.dp,
                            color = if (isChecked) RefPrimary else RefPrimary.copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isChecked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = RefDeepGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            if (prayerType != PrayerType.entries.last()) Spacer(Modifier.height(4.dp))
        }
    }
}
