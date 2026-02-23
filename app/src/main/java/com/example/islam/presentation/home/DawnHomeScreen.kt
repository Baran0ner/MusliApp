package com.example.islam.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
    prayerName: String = "Sabah",
    time: String = "06:19",
    countdown: String = "Sonraki vakte 8s 33dk",
    gregorianDate: String = "21 Şubat 2026, Cumartesi",
    hijriDate: String = "4 رمضان 1447",
    verseText: String = "\"Allah, O'ndan başka ilah olmayandır. Diridir, kayyumdur. O'nu ne uyuklama ne de uyku tutar. Göklerde ve yerde ne varsa hepsi O'nundur.\"",
    verseRef: String = "Bakara, 255 (Âyetü'l-Kürsî)",
    prayerItems: List<PrayerDisplayItem> = defaultPrayerItems(),
    onQiblaClick: () -> Unit = {},
    onTasbihClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 96.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Üst: Tarih + Hicri + Namaz adı + Saat + Geri sayım (kompakt, ayete yer bırakır) ───────
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(4.dp))
                    // Miladi tarih — uppercase, küçük, gri
                    Text(
                        text = gregorianDate.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        color = RefTextGray300
                    )
                    Spacer(Modifier.height(10.dp))
                    // Saat — büyük ama ekranı doldurmayacak kadar
                    Text(
                        text = time,
                        fontSize = 64.sp,
                        lineHeight = 64.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Serif,
                        letterSpacing = (-0.5).sp,
                        color = Color.White
                    )
                    // Geri sayım pill
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(RefSurfaceCard.copy(alpha = 0.4f))
                            .border(1.dp, RefBorderPrimary30, RoundedCornerShape(50.dp))
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = RefPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = countdown,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = RefTextGray200
                        )
                    }
                }

                // Üstte esnek boşluk: ayet + Kıble + vakitler blokunu alta, menüye yaklaştırır
                Spacer(Modifier.weight(1f))

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
                            .background(RefSurfaceCard60)
                            .border(1.dp, RefBorderPrimary30, RoundedCornerShape(32.dp))
                            .padding(horizontal = 20.dp, vertical = 14.dp)
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
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 20.sp,
                            color = RefTextGray100.copy(alpha = 0.95f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = verseRef.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.2.sp,
                            color = RefPrimary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                // Ayet ile Kıble/Tespih arasında boşluk
                Spacer(Modifier.height(16.dp))
                // ── Kıble & Tespih (alttaki menüye yakın) ───────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KibleButton(onClick = onQiblaClick, modifier = Modifier.weight(1f))
                    TespihButton(onClick = onTasbihClick, modifier = Modifier.weight(1f))
                }
                // Kıble/Tespih ile namaz vakitleri arasında boşluk
                Spacer(Modifier.height(12.dp))
                // ── Namaz vakitleri satırı ─
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    prayerItems.forEach { item ->
                        PrayerTimeChip(item = item)
                    }
                }
            }
        }
    }
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
    androidx.compose.material3.Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, RefBorderPrimary40, RoundedCornerShape(24.dp)),
        color = Color.Transparent,
        onClick = onClick,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
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
                text = "Kıble",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = RefTextGray200
            )
        }
    }
}

@Composable
private fun TespihButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, RefBorderPrimary40, RoundedCornerShape(24.dp)),
        color = Color.Transparent,
        onClick = onClick,
        shape = RoundedCornerShape(24.dp)
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp, horizontal = 16.dp),
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
            text = "Tespih",
            fontSize = 16.sp,
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
            RefTextGray300,
            RefTextGray400
        )
    }
    Column(
        modifier = Modifier
            .then(if (item.isActive) Modifier else Modifier.alpha(0.7f))
            .widthIn(min = 64.dp)
            .clip(RoundedCornerShape(24.dp))
            .then(
                if (item.isActive) Modifier.shadow(8.dp, RoundedCornerShape(24.dp), spotColor = RefPrimary.copy(alpha = 0.15f))
                else Modifier
            )
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
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
            fontSize = 10.sp,
            fontWeight = if (item.isActive) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.5.sp,
            color = nameColor
        )
        Spacer(Modifier.height(2.dp))
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
