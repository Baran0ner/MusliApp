package com.example.islam.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.islam.R
import com.example.islam.core.i18n.LocalStrings
import java.util.Calendar

private val CardBg = Color(0xFF143225)
private val CardBorder = Color(0x5CD4AF37)
private val CardShadow = Color(0x80101511)
private val TitleColor = Color(0xFFF3F7F2)
private val SubtitleColor = Color(0xFFAEC0B4)
private val BadgeBg = Color(0xFF1A3D2E)
private val BadgeBorder = Color(0x40D4AF37)
private val DayLabelColor = Color(0xFF9FB2A6)
private val ActiveCircleColor = Color(0xFFD4AF37)
private val ActiveCircleBorder = Color(0xFFF5C842)
private val InactiveCircleColor = Color(0x244D6458)
private val InactiveCircleBorder = Color(0x50698274)
private val ActiveCheckColor = Color(0xFFFFFFFF)
private val InactiveCheckColor = Color(0x708BA196)
private val ProgressTrackColor = Color(0x33658274)
private val ProgressFillColor = Color(0xFFE3BE59)
private val ProgressBorder = Color(0x4DD4AF37)

@Composable
fun StreakReferenceCard(
    streakDays: Int,
    completedToday: Int,
    dailyGoal: Int,
    weeklyVisitMask: Int? = null,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    val weekLabels = if (strings.streakWeekLabels.size == 7) {
        strings.streakWeekLabels
    } else {
        listOf("M", "Tu", "W", "Th", "F", "Sa", "Su")
    }
    val safeGoal = dailyGoal.coerceAtLeast(1)
    val todayCompleted = completedToday.coerceAtLeast(0) >= safeGoal
    val todayIndex = remember { currentWeekIndex() }
    val dayStates = remember(streakDays, todayCompleted, todayIndex, weeklyVisitMask) {
        if (weeklyVisitMask != null) {
            List(7) { idx -> (weeklyVisitMask and (1 shl idx)) != 0 }
        } else {
            buildWeeklyStreakStates(
                streakDays = streakDays,
                todayCompleted = todayCompleted,
                todayIndex = todayIndex
            )
        }
    }
    val completedCount = dayStates.count { it }
    val progress = (completedCount / 7f).coerceIn(0f, 1f)
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 420),
        label = "streak_progress"
    ).value

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val scale = (maxWidth.value / 340f).coerceIn(0.88f, 1.18f)
        val corner = 28.dp * scale
        val badgeSize = 90.dp * scale
        val circleSize = 30.dp * scale
        val checkSize = 18.dp * scale
        val compactWeekRow = maxWidth < 320.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(corner), spotColor = CardShadow)
                .clip(RoundedCornerShape(corner))
                .background(CardBg)
                .border(1.dp, CardBorder, RoundedCornerShape(corner))
                .padding(horizontal = 16.dp * scale, vertical = 16.dp * scale)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StarBadge(
                    streakDays = streakDays.coerceAtLeast(0),
                    badgeSize = badgeSize
                )

                Column(
                    modifier = Modifier
                        .padding(start = 10.dp * scale)
                        .weight(1f)
                ) {
                    Text(
                        text = strings.streakCardTitle,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = (26f * scale).sp,
                            lineHeight = (28f * scale).sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = TitleColor
                    )
                    Spacer(Modifier.height(6.dp * scale))
                    Text(
                        text = strings.streakCardDescription,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (14f * scale).sp,
                            lineHeight = (20f * scale).sp
                        ),
                        fontWeight = FontWeight.Medium,
                        color = SubtitleColor
                    )
                }
            }

            Spacer(Modifier.height(13.dp * scale))

            if (compactWeekRow) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .testTag("streak_day_row"),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekLabels.forEachIndexed { index, label ->
                        DayCircle(
                            label = label,
                            isActive = dayStates[index],
                            circleSize = circleSize,
                            checkSize = checkSize,
                            modifier = Modifier.widthIn(min = 36.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("streak_day_row"),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekLabels.forEachIndexed { index, label ->
                        DayCircle(
                            label = label,
                            isActive = dayStates[index],
                            circleSize = circleSize,
                            checkSize = checkSize,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp * scale))
            StreakProgressBar(
                progress = animatedProgress,
                doneCount = completedCount,
                totalCount = 7,
                scale = scale,
                label = strings.streakWeeklyProgress
            )
        }
    }
}

@Composable
private fun StarBadge(
    streakDays: Int,
    badgeSize: Dp
) {
    val numberSize = 22f * (badgeSize.value / 94f)

    Box(
        modifier = Modifier.size(badgeSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(badgeSize * 0.92f)
                .clip(CircleShape)
                .background(BadgeBg)
                .border(1.dp, BadgeBorder, CircleShape)
        )
        Image(
            painter = painterResource(R.drawable.ic_streak_premium),
            contentDescription = null,
            modifier = Modifier.size(badgeSize * 0.72f)
        )
        Text(
            text = streakDays.toString(),
            color = Color.White.copy(alpha = 0.96f),
            fontSize = numberSize.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.offset(y = 7.dp * (badgeSize.value / 94f))
        )
    }
}

@Composable
private fun DayCircle(
    label: String,
    isActive: Boolean,
    circleSize: Dp,
    checkSize: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = DayLabelColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(if (isActive) ActiveCircleColor else InactiveCircleColor)
                .border(
                    width = 1.dp,
                    color = if (isActive) ActiveCircleBorder else InactiveCircleBorder,
                    shape = CircleShape
                )
                .then(
                    if (isActive) Modifier.shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = ActiveCircleColor.copy(alpha = 0.35f)
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = if (isActive) ActiveCheckColor else InactiveCheckColor,
                modifier = Modifier.size(checkSize)
            )
        }
    }
}

@Composable
private fun StreakProgressBar(
    progress: Float,
    doneCount: Int,
    totalCount: Int,
    scale: Float,
    label: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp * scale)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = (12f * scale).sp),
                color = DayLabelColor
            )
            Text(
                text = "$doneCount/$totalCount",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = (12f * scale).sp),
                fontWeight = FontWeight.SemiBold,
                color = ProgressFillColor
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp * scale)
                .clip(RoundedCornerShape(999.dp))
                .background(ProgressTrackColor)
                .border(1.dp, ProgressBorder, RoundedCornerShape(999.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(8.dp * scale)
                    .clip(RoundedCornerShape(999.dp))
                    .background(ProgressFillColor)
            )
        }
    }
}

private fun buildWeeklyStreakStates(
    streakDays: Int,
    todayCompleted: Boolean,
    todayIndex: Int
): List<Boolean> {
    val states = MutableList(7) { false }
    val safeStreak = streakDays.coerceAtLeast(0)
    val completedDays = if (todayCompleted) safeStreak else (safeStreak - 1).coerceAtLeast(0)
    repeat(minOf(completedDays, 7)) { shift ->
        val index = mod(todayIndex - shift, 7)
        states[index] = true
    }
    if (!todayCompleted) {
        states[todayIndex] = false
    }
    return states
}

private fun currentWeekIndex(): Int {
    return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6
        else -> 0
    }
}

private fun mod(value: Int, base: Int): Int = ((value % base) + base) % base
