package com.example.islam.presentation.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.islam.R
import com.example.islam.core.navigation.Screen
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Renkler
// ─────────────────────────────────────────────────────────────────────────────
private val Accent      = Color(0xFF5B9CF6)
private val AccentLight = Color(0xFF93C5FD)
private val BgDark      = Color(0xFF0F172A)
private val White20     = Color(0x33FFFFFF)
private val White50     = Color(0x80FFFFFF)
private val White60     = Color(0x99FFFFFF)
private val White80     = Color(0xCCFFFFFF)
private val Slate100    = Color(0xFFF1F5F9)
private val Slate300    = Color(0xFFCBD5E1)

// ─────────────────────────────────────────────────────────────────────────────
// Ana ekran
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun OnboardingScreen(
    navController : NavController,
    viewModel     : OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState { 3 }
    val scope      = rememberCoroutineScope()

    fun finishOnboarding() {
        viewModel.completeOnboarding()
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Onboarding.route) { inclusive = true }
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { finishOnboarding() }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        else
            finishOnboarding()
    }

    fun onContinue() {
        if (pagerState.currentPage < 2)
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
        else
            locationLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
    }

    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
        when (page) {

            // ── SAYFA 1 ──────────────────────────────────────────────────────
            0 -> OnboardingPageLayout(
                backgroundRes  = R.drawable.onb1,
                gradientColors = arrayOf(
                    0.00f to Color(0xB3000000),
                    0.50f to Color(0x33000000),
                    1.00f to Color(0xCC000000)
                ),
                leftLabel      = "EZAN VAKTİ",
                showCloseBtn   = true,
                titleLine1     = "Vakti Geldiğinde",
                titleLine2     = "Huzura Durun",
                description    = "Nerede olursanız olun, en hassas hesaplamalarla namaz vakitlerinden anında haberdar olun.",
                pageIndex      = 0,
                buttonText     = "Devam Et",
                isLastPage     = false,
                onContinue     = ::onContinue,
                onSkip         = ::finishOnboarding
            )

            // ── SAYFA 2 ──────────────────────────────────────────────────────
            1 -> OnboardingPageLayout(
                backgroundRes  = R.drawable.onb2,
                gradientColors = arrayOf(
                    0.00f to BgDark.copy(alpha = 0.80f),
                    0.45f to Color(0x4D000000),
                    1.00f to BgDark.copy(alpha = 0.95f)
                ),
                leftLabel      = "",
                showCloseBtn   = false,
                titleLine1     = "İlahi Kelam",
                titleLine2     = "Her An Sizinle",
                description    = "Gelişmiş okuma ekranı ve farklı hafızlardan dinleme seçenekleriyle Kur'an'ı ruhunuzda hissedin.",
                pageIndex      = 1,
                buttonText     = "Devam Et",
                isLastPage     = false,
                onContinue     = ::onContinue,
                onSkip         = ::finishOnboarding
            )

            // ── SAYFA 3 ──────────────────────────────────────────────────────
            2 -> OnboardingPageLayout(
                backgroundRes  = R.drawable.onb3,
                gradientColors = arrayOf(
                    0.00f to Color(0x660A0904),
                    1.00f to Color(0xCC0A0904)
                ),
                leftLabel      = "",
                showCloseBtn   = false,
                titleLine1     = "Manevi Yolculuğunuz",
                titleLine2     = "Başlıyor",
                description    = "Vakitleri tam zamanında hatırlatabilmemiz ve kıbleyi doğru bulabilmemiz için izinlerinize ihtiyacımız var.",
                pageIndex      = 2,
                buttonText     = "Başla",
                isLastPage     = true,
                onContinue     = ::onContinue,
                onSkip         = ::finishOnboarding
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  ORTAK SAYFA LAYOUT
//  ┌─────────────────────────────────┐
//  │ [leftLabel]           [Atla / X] │  ← sabit header (pt=52dp)
//  │                                  │
//  │       Başlık satır 1             │  ← weight(1f) → dikey merkez
//  │       Başlık satır 2 (mavi)      │
//  │       Açıklama                   │
//  │                                  │
//  │  ●─────  ●  ●  (pagination)      │  ← sabit alt (pb=52dp)
//  │  [        Devam Et →         ]   │
//  │           Atla                   │
//  └─────────────────────────────────┘
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun OnboardingPageLayout(
    backgroundRes  : Int,
    gradientColors : Array<Pair<Float, Color>>,
    leftLabel      : String,       // üst sol metin ("EZAN VAKTİ" veya boş)
    showCloseBtn   : Boolean,      // üst sağda X mi, yoksa "Atla" mı
    titleLine1     : String,
    titleLine2     : String,
    description    : String,
    pageIndex      : Int,
    buttonText     : String,
    isLastPage     : Boolean,
    onContinue     : () -> Unit,
    onSkip         : () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // ── Arka plan ────────────────────────────────────────────────────────
        Image(
            painter            = painterResource(backgroundRes),
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colorStops = gradientColors))
        )

        // ── İçerik ───────────────────────────────────────────────────────────
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── HEADER — sabit yükseklik (48dp içerik + 52dp padding top) ────
            // Her sayfada aynı yüksekliği tutar → alt içerik hizalanır
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp)
                    .height(32.dp)             // ← sabit! Sayfa fark etmez
            ) {
                // Sol: etiket (varsa)
                if (leftLabel.isNotEmpty()) {
                    Text(
                        text          = leftLabel,
                        fontSize      = 12.sp,
                        fontWeight    = FontWeight.Medium,
                        color         = White50,
                        letterSpacing = 3.sp,
                        modifier      = Modifier.align(Alignment.CenterStart)
                    )
                }
                // Sağ: X kapatma veya "Atla"
                if (showCloseBtn) {
                    Icon(
                        imageVector        = Icons.Outlined.Close,
                        contentDescription = "Kapat",
                        tint               = White80,
                        modifier           = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterEnd)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = ripple(bounded = false, color = Color.White)
                            ) { onSkip() }
                    )
                } else {
                    Text(
                        text       = "Atla",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color      = White60,
                        modifier   = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication        = ripple(bounded = false, color = Color.White)
                            ) { onSkip() }
                    )
                }
            }

            // ── ORTA — weight(1f), içeriği dikey ortalar ─────────────────────
            Column(
                modifier            = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text       = titleLine1,
                    fontSize   = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Slate100,
                    textAlign  = TextAlign.Center,
                    lineHeight = 44.sp
                )
                Text(
                    text       = titleLine2,
                    fontSize   = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = AccentLight,
                    textAlign  = TextAlign.Center,
                    lineHeight = 44.sp
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text       = description,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color      = Slate300,
                    textAlign  = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }

            // ── ALT — sabit yükseklik, her sayfada aynı hizada ───────────────
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 52.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pagination
                PageIndicator(pageCount = 3, currentPage = pageIndex)

                // Ana buton — "Devam Et →" veya "Başla →"
                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.horizontalGradient(listOf(Accent, Color(0xFF3B82F6)))
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = ripple(color = Color.White)
                        ) { onContinue() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text       = buttonText,
                            fontSize   = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color.White
                        )
                        Icon(
                            imageVector        = Icons.Outlined.ArrowForward,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(20.dp)
                        )
                    }
                }

                // Sayfa 3: güvenlik notu
                if (isLastPage) {
                    Text(
                        text          = "GİZLİLİĞİNİZ VE GÜVENLİĞİNİZ ÖNCELİĞİMİZDİR",
                        fontSize      = 9.sp,
                        fontWeight    = FontWeight.Medium,
                        color         = Color(0xFF64748B).copy(alpha = 0.7f),
                        letterSpacing = 1.5.sp,
                        textAlign     = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pagination göstergesi
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(pageCount) { idx ->
            val isActive = idx == currentPage
            val width by animateDpAsState(
                targetValue   = if (isActive) 24.dp else 6.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label         = "dot_$idx"
            )
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(if (isActive) Accent else White20)
            )
        }
    }
}
