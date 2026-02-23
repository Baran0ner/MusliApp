package com.example.islam.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.islam.R
import androidx.compose.ui.unit.sp
import com.example.islam.core.i18n.LocalStrings
import com.example.islam.presentation.components.StreakReferenceCard
import com.example.islam.ui.theme.AmiriFamily

// ─────────────────────────────────────────────────────────────────────────────
// Referans HTML/CSS renkleri (Tailwind config)
// ─────────────────────────────────────────────────────────────────────────────
private val RefPrimary = Color(0xFFD4AF37)           // Gold
private val RefBackgroundDark = Color(0xFF0B2419)   // Deep Emerald Green
private val RefSurfaceCard = Color(0xFF133326)      // Card background
private val RefSurfaceCard60 = Color(0x99133326)    // surface-card/60
private val RefBorderPrimary30 = Color(0x4DD4AF37)  // primary/30
private val RefBorderPrimary40 = Color(0x66D4AF37)  // primary/40
private val RefBorderPrimary50 = Color(0x80D4AF37)  // primary/50
private val RefBorderWhite5 = Color(0x0DFFFFFF)     // white/5
private val RefTextGray100 = Color(0xFFF3F4F6)      // gray-100
private val RefTextGray200 = Color(0xFFE5E7EB)      // gray-200
private val RefTextGray300 = Color(0xFFD1D5DB)      // gray-300
private val RefTextGray400 = Color(0xFF9CA3AF)      // gray-400
private val RefPrimary70 = Color(0xB3D4AF37)       // primary/70
private val RefSurfaceElevated = Color(0xFF183829)
private val RefChipPassive = Color(0xFF132C22)

// ─────────────────────────────────────────────────────────────────────────────
// Veri modeli (mevcut)
// ─────────────────────────────────────────────────────────────────────────────
data class PrayerDisplayItem(
    val name: String,
    val time: String,
    val icon: ImageVector,
    val iconTint: Color,
    val isActive: Boolean = false,
    val iconResId: Int? = null
)

// ─────────────────────────────────────────────────────────────────────────────
// DawnHomeScreen — Referans ekran görüntüsüne birebir uyumlu
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DawnHomeScreen(
    greetingText: String = "Assalamu Aleykum",
    prayerName: String = "Sabah",
    time: String = "06:19",
    countdown: String = "Sonraki vakte 8s 33dk",
    gregorianDate: String = "21 Şubat 2026, Cumartesi",
    hijriDate: String = "4 رمضان 1447",
    verseText: String = "\"Allah, O'ndan başka ilah olmayandır. Diridir, kayyumdur. O'nu ne uyuklama ne de uyku tutar. Göklerde ve yerde ne varsa hepsi O'nundur.\"",
    verseRef: String = "Bakara, 255 (Âyetü'l-Kürsî)",
    prayerItems: List<PrayerDisplayItem> = defaultPrayerItems(),
    streakDays: Int = 12,
    completedToday: Int = 3,
    dailyGoal: Int = 5,
    weeklyVisitMask: Int = 0,
    streakCongratsText: String? = null,
    showNamePromptCard: Boolean = false,
    onSaveName: (String) -> Unit = {},
    onSkipNamePrompt: () -> Unit = {},
    onQiblaClick: () -> Unit = {},
    onTasbihClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .testTag("home_main_scroll"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Üst: Tarih + Hicri + Namaz adı + Saat + Geri sayım (kompakt, ayete yer bırakır) ───────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = greetingText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        letterSpacing = 0.2.sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = RefTextGray200
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = gregorianDate.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 12.sp,
                        letterSpacing = 0.4.sp
                    ),
                    fontWeight = FontWeight.Medium,
                    color = RefTextGray300
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = time,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 56.sp,
                        lineHeight = 58.sp,
                        letterSpacing = (-0.6).sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = prayerName.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.7.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = RefPrimary70
                )
                Row(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(RefSurfaceElevated.copy(alpha = 0.95f))
                        .border(1.dp, RefBorderPrimary40, RoundedCornerShape(50.dp))
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = RefPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = countdown,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = RefPrimary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            if (showNamePromptCard) {
                HomeNamePromptCard(
                    onSaveName = onSaveName,
                    onSkip = onSkipNamePrompt
                )
                Spacer(Modifier.height(14.dp))
            }
            StreakTopCard(
                streakDays = streakDays,
                completedToday = completedToday,
                dailyGoal = dailyGoal,
                weeklyVisitMask = weeklyVisitMask
            )
            if (!streakCongratsText.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = streakCongratsText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = RefPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(18.dp))

            // ── Ayet kartı: yükseklik ayet uzunluğuna göre değişir ──────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 2.dp)
            ) {
                // Arka plan glow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(RefPrimary.copy(alpha = 0.1f))
                        .graphicsLayer { alpha = 0.6f }
                )
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(RefSurfaceElevated.copy(alpha = 0.88f))
                        .border(1.dp, RefBorderPrimary30, RoundedCornerShape(32.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FormatQuote,
                        contentDescription = null,
                        tint = RefPrimary70,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(18.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = verseText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        ),
                        fontWeight = FontWeight.Medium,
                        color = RefTextGray100.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = verseRef.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp,
                        color = RefPrimary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                KibleButton(onClick = onQiblaClick, modifier = Modifier.weight(1f))
                TespihButton(onClick = onTasbihClick, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .testTag("home_prayer_chips_row")
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            prayerItems.forEach { item ->
                PrayerTimeChip(item = item)
            }
        }
    }
}

@Composable
private fun HomeNamePromptCard(
    onSaveName: (String) -> Unit,
    onSkip: () -> Unit
) {
    val strings = LocalStrings.current
    var input by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(RefSurfaceCard60)
            .border(1.dp, RefBorderPrimary30, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = strings.namePromptTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = RefTextGray100
        )
        Text(
            text = strings.namePromptDescription,
            style = MaterialTheme.typography.bodySmall,
            color = RefTextGray300
        )
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            singleLine = true,
            label = { Text(strings.nameInputLabel) },
            placeholder = { Text(strings.nameInputPlaceholder) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RefPrimary,
                unfocusedBorderColor = RefBorderPrimary40,
                focusedTextColor = RefTextGray100,
                unfocusedTextColor = RefTextGray100,
                focusedLabelColor = RefPrimary,
                unfocusedLabelColor = RefTextGray300
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onSkip) {
                Text(text = strings.nameSkip, color = RefTextGray300)
            }
            Button(
                onClick = { onSaveName(input) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = RefPrimary,
                    contentColor = RefBackgroundDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = strings.nameSave, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StreakTopCard(
    streakDays: Int,
    completedToday: Int,
    dailyGoal: Int,
    weeklyVisitMask: Int
) {
    StreakReferenceCard(
        streakDays = streakDays,
        completedToday = completedToday,
        dailyGoal = dailyGoal,
        weeklyVisitMask = weeklyVisitMask
    )
}

@Composable
private fun HijriDateText(hijriDate: String) {
    val parts = hijriDate.trim().split(" ", limit = 2)
    val dayPart = parts.getOrNull(0) ?: ""
    val restPart = if (parts.size > 1) " ${parts[1]}" else ""
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = RefPrimary, fontFamily = AmiriFamily)) {
                append(dayPart)
            }
            withStyle(SpanStyle(color = RefTextGray300, fontFamily = AmiriFamily)) {
                append(restPart)
            }
        },
        fontSize = 18.sp,
        fontFamily = AmiriFamily
    )
}

@Composable
private fun KibleButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    androidx.compose.material3.Surface(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, RefBorderPrimary40, RoundedCornerShape(26.dp)),
        color = Color.Transparent,
        onClick = onClick,
        shape = RoundedCornerShape(26.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RefSurfaceCard)
                    .border(1.dp, RefBorderPrimary40, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_kible),
                    contentDescription = "Kıble",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = strings.navQibla,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = RefTextGray200
            )
        }
    }
}

@Composable
private fun TespihButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val strings = LocalStrings.current
    androidx.compose.material3.Surface(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .border(1.dp, RefBorderPrimary40, RoundedCornerShape(26.dp)),
        color = Color.Transparent,
        onClick = onClick,
        shape = RoundedCornerShape(26.dp)
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(RefSurfaceCard)
                .border(1.dp, RefBorderPrimary40, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.icon_tespih),
                contentDescription = "Tespih",
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = strings.navDhikr,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = RefTextGray200
        )
    }
    }
}

@Composable
private fun PrayerTimeChip(item: PrayerDisplayItem) {
    val (bg, borderColor, contentColor, nameColor) = if (item.isActive) {
        PrayerChipColors(
            RefSurfaceCard,
            RefBorderPrimary50,
            Color.White,
            RefPrimary
        )
    } else {
        PrayerChipColors(
            RefSurfaceCard.copy(alpha = 0.3f),
            RefBorderWhite5,
            RefTextGray200.copy(alpha = 0.86f),
            RefTextGray300.copy(alpha = 0.88f)
        )
    }
    Column(
        modifier = Modifier
            .then(if (item.isActive) Modifier else Modifier.alpha(0.7f))
            .width(72.dp)
            .heightIn(min = 124.dp)
            .clip(RoundedCornerShape(24.dp))
            .then(
                if (item.isActive) Modifier.shadow(8.dp, RoundedCornerShape(24.dp), spotColor = RefPrimary.copy(alpha = 0.15f))
                else Modifier
            )
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (item.iconResId != null) {
            Image(
                painter = painterResource(item.iconResId),
                contentDescription = item.name,
                modifier = Modifier
                    .size(28.dp)
                    .then(if (item.isActive) Modifier else Modifier.alpha(0.45f))
            )
        } else {
            Icon(
                imageVector = item.icon,
                contentDescription = item.name,
                tint = if (item.isActive) RefPrimary else RefTextGray400,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = item.name.uppercase(),
            fontSize = 11.sp,
            fontWeight = if (item.isActive) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.5.sp,
            color = nameColor
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = item.time,
            fontSize = 14.sp,
            fontWeight = if (item.isActive) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor
        )
    }
}

private data class PrayerChipColors(
    val bg: Color,
    val borderColor: Color,
    val contentColor: Color,
    val nameColor: Color
)

// ─────────────────────────────────────────────────────────────────────────────
// Varsayılan namaz öğeleri (referans ikonlarına uygun)
// ─────────────────────────────────────────────────────────────────────────────
fun defaultPrayerItems() = listOf(
    PrayerDisplayItem(
        name = "Sabah",
        time = "06:19",
        icon = Icons.Outlined.DarkMode,
        iconTint = RefPrimary,
        isActive = true,
        iconResId = R.drawable.icon_sabah
    ),
    PrayerDisplayItem(
        name = "Öğle",
        time = "13:23",
        icon = Icons.Outlined.WbSunny,
        iconTint = RefTextGray400,
        isActive = false,
        iconResId = R.drawable.icon_ogle
    ),
    PrayerDisplayItem(
        name = "İkindi",
        time = "16:21",
        icon = Icons.Outlined.WbTwilight,
        iconTint = RefTextGray400,
        isActive = false,
        iconResId = R.drawable.icon_ikindi
    ),
    PrayerDisplayItem(
        name = "Akşam",
        time = "18:53",
        icon = Icons.Outlined.WbTwilight,
        iconTint = RefTextGray400,
        isActive = false,
        iconResId = R.drawable.icon_aksam
    ),
    PrayerDisplayItem(
        name = "Yatsı",
        time = "20:12",
        icon = Icons.Outlined.Brightness2,
        iconTint = RefTextGray400,
        isActive = false,
        iconResId = R.drawable.icon_yatsi
    )
)

@Preview(name = "Dawn Home - Referans", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DawnHomeScreenPreview() {
    DawnHomeScreen()
}
