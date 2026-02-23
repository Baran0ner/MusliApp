package com.example.islam.presentation.ramadan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.islam.domain.model.PrayerType
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PlannerBgTop = Color(0xFF0F2B1F)
private val PlannerBgBottom = Color(0xFF091811)
private val PlannerCard = Color(0xFF153527)
private val PlannerBorder = Color(0x4DD4AF37)
private val PlannerGold = Color(0xFFD4AF37)
private val PlannerText = Color(0xFFF3F7F2)
private val PlannerMuted = Color(0xFFAEC0B4)

@Composable
fun RamadanPlannerScreen(
    navController: NavController,
    viewModel: RamadanPlannerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PlannerBgTop, PlannerBgBottom)))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MonthHeader(
                selectedMonth = state.selectedMonth,
                cityLabel = state.cityLabel,
                onPrev = viewModel::previousMonth,
                onNext = viewModel::nextMonth
            )

            Button(
                onClick = viewModel::completeToday,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Bugünü Tamamla", fontWeight = FontWeight.Bold)
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PlannerGold)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.days, key = { it.dateIso }) { day ->
                        PlannerDayCard(
                            day = day,
                            onToggleFast = { checked -> viewModel.toggleFast(day.dateIso, checked) },
                            onTogglePrayer = { prayerType, checked ->
                                viewModel.togglePrayer(day.dateIso, prayerType, checked)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    selectedMonth: YearMonth,
    cityLabel: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    val monthText = selectedMonth.atDay(1)
        .format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("tr", "TR")))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    Surface(
        color = PlannerCard,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PlannerBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = null,
                    tint = PlannerGold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onPrev)
                        .padding(4.dp)
                )
                Text(text = monthText, color = PlannerText, style = MaterialTheme.typography.titleLarge)
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = PlannerGold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onNext)
                        .padding(4.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = PlannerGold,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "  $cityLabel",
                    color = PlannerMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PlannerDayCard(
    day: RamadanPlannerDayUi,
    onToggleFast: (Boolean) -> Unit,
    onTogglePrayer: (PrayerType, Boolean) -> Unit
) {
    val doneColor = if (day.goalReached) PlannerGold else PlannerMuted

    Surface(
        color = PlannerCard.copy(alpha = if (day.isToday) 0.98f else 0.92f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(152.dp)
            .border(
                width = if (day.isToday) 1.5.dp else 1.dp,
                color = if (day.isToday) PlannerGold.copy(alpha = 0.7f) else PlannerBorder,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day.day.toString(),
                    color = PlannerText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (day.fastDone) Icons.Outlined.Check else Icons.Outlined.Favorite,
                        contentDescription = null,
                        tint = if (day.fastDone) PlannerGold else PlannerMuted,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .clickable { onToggleFast(!day.fastDone) }
                    )
                    Text(
                        text = if (day.fastDone) "  Oruç ✓" else "  Oruç",
                        color = PlannerMuted,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Text(
                text = day.hijriDate,
                color = PlannerMuted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("İmsak", color = PlannerMuted, style = MaterialTheme.typography.labelSmall)
                    Text(day.fajr, color = PlannerText, style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Akşam", color = PlannerMuted, style = MaterialTheme.typography.labelSmall)
                    Text(day.maghrib, color = PlannerText, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Namaz ${day.prayedCount}/${day.dailyGoal}",
                    color = doneColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    PrayerType.entries.forEach { type ->
                        val active = when (type) {
                            PrayerType.FAJR -> day.fajrDone
                            PrayerType.DHUHR -> day.dhuhrDone
                            PrayerType.ASR -> day.asrDone
                            PrayerType.MAGHRIB -> day.maghribDone
                            PrayerType.ISHA -> day.ishaDone
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (active) PlannerGold else PlannerMuted.copy(alpha = 0.35f))
                                .clickable { onTogglePrayer(type, !active) }
                        )
                    }
                }
            }
        }
    }
}
