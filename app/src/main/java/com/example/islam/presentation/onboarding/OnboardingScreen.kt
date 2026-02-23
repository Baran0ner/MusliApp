package com.example.islam.presentation.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.islam.R
import com.example.islam.core.i18n.LocalStrings
import com.example.islam.core.navigation.Screen
import com.example.islam.presentation.auth.AuthState
import com.example.islam.presentation.auth.GoogleAuthViewModel
import com.example.islam.presentation.components.ProminentDisclosureDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ─────────────────────────────────────────────────────────────────────────────
// Nur-u İslam Renk Paleti
// ─────────────────────────────────────────────────────────────────────────────
private val Gold        = Color(0xFFD4AF37)
private val GoldMuted   = Color(0xFFC5A028)
private val GoldDim     = Color(0x66D4AF37)
private val BgDeep      = Color(0xFF052E25)   // Very Dark Green
private val BgDarker    = Color(0xFF021F18)   // Even Darker Green
private val EmeraldDeep = Color(0xFF062C21)
private val GrayText    = Color(0xFFD1D5DB)   // gray-300
private val GoldGlow    = Color(0x66D4AF37)

private fun googleSignInErrorMessage(e: ApiException): String {
    return when (e.statusCode) {
        GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Giriş iptal edildi."
        GoogleSignInStatusCodes.NETWORK_ERROR -> "Ağ hatası. İnternet bağlantısını kontrol edin."
        GoogleSignInStatusCodes.DEVELOPER_ERROR -> "Google giriş yapılandırması hatalı (SHA-1 / paket adı)."
        GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Google ile giriş başarısız."
        else -> "Google giriş hatası (kod: ${e.statusCode})."
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Ana Ekran
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun OnboardingScreen(
    navController : NavController,
    viewModel     : OnboardingViewModel = hiltViewModel(),
    authViewModel : GoogleAuthViewModel = hiltViewModel()
) {
    var showDisclosure by rememberSaveable { mutableStateOf(true) }
    var currentPage by rememberSaveable { mutableIntStateOf(0) }
    val scope      = rememberCoroutineScope()
    val context    = LocalContext.current
    val authState  by authViewModel.authState.collectAsState()
    val strings = LocalStrings.current
    var displayName by rememberSaveable { mutableStateOf("") }

    // ── İzin launcher'ları ───────────────────────────────────────────────────
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

    // ── Google Sign-In ────────────────────────────────────────────────────────
    val webClientId = remember { context.getString(R.string.default_web_client_id) }
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) {
            if (result.resultCode != android.app.Activity.RESULT_CANCELED) {
                authViewModel.setError("Google ile giriş başarısız.")
            }
            return@rememberLauncherForActivityResult
        }
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    authViewModel.signInWithGoogle(idToken)
                } else {
                    authViewModel.setError("ID token alınamadı. Google yapılandırmasını kontrol edin.")
                }
            } catch (e: ApiException) {
                authViewModel.setError(googleSignInErrorMessage(e))
            }
        }
        // RESULT_CANCELED → sayfada kal
    }

    // ── Auth başarılıysa izinlere geç ────────────────────────────────────────
    // NOT: resetState() sonra çağrılmalı — önce çağırılırsa LaunchedEffect yeniden
    // tetiklenir ve locationLauncher.launch() asla çalışmaz.
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            locationLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
            authViewModel.resetState()
        }
    }

    fun onContinue() {
        if (currentPage < 3) {
            currentPage++
        } else {
            scope.launch {
                try { googleClient.signOut().await() } catch (_: Exception) { }
                googleLauncher.launch(googleClient.signInIntent)
            }
        }
    }

    fun continueWithoutAccount() {
        authViewModel.resetState()
        locationLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    // ── Animasyonlu sayfa geçişi ──────────────────────────────────────────────
    var previousPage by remember { mutableIntStateOf(0) }

    AnimatedContent(
        targetState = currentPage,
        transitionSpec = {
            val goingForward = targetState > initialState
            val enter = slideInHorizontally(
                initialOffsetX = { if (goingForward) it else -it },
                animationSpec  = tween(400)
            ) + fadeIn(animationSpec = tween(400))
            val exit = slideOutHorizontally(
                targetOffsetX = { if (goingForward) -it else it },
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(300))
            enter togetherWith exit
        },
        modifier = Modifier.fillMaxSize(),
        label    = "onboarding_page"
    ) { page ->
        // previousPage güncelle (SideEffect — composable içinde state değişimi olmadan)
        SideEffect { previousPage = page }
        when (page) {
            0 -> NurOnboardingPage(
                imageRes     = R.drawable.tespih,
                title        = "MANEVİ HUZUR",
                description  = "Günlük zikirlerinizi ve dualarınızı takip ederek manevi huzura erişin.",
                pageIndex    = 0,
                pageCount    = 4,
                isLastPage   = false,
                isLoading    = false,
                errorMessage = null,
                onContinue   = ::onContinue
            )
            1 -> NurOnboardingPage(
                imageRes     = R.drawable.kuran,
                title        = "KUR'AN-I KERİM",
                description  = "Her ayette bir huzur, her kelimede bir rehber bulun.",
                pageIndex    = 1,
                pageCount    = 4,
                isLastPage   = false,
                isLoading    = false,
                errorMessage = null,
                onContinue   = ::onContinue
            )
            2 -> NameOnboardingPage(
                pageIndex = 2,
                pageCount = 4,
                value = displayName,
                onValueChange = { displayName = it },
                onSave = {
                    viewModel.saveDisplayName(displayName)
                    currentPage = 3
                },
                onSkip = { currentPage = 3 },
                strings = strings
            )
            else -> NurOnboardingPage(
                imageRes     = R.drawable.cami,
                title        = "MANEVİ MEKANLAR",
                description  = "Hesabınızla giriş yaparak namaz streak'inizi senkronize edin ve Ramazan bildirimlerini alın.",
                pageIndex    = 3,
                pageCount    = 4,
                isLastPage   = true,
                isLoading    = authState is AuthState.Loading,
                errorMessage = (authState as? AuthState.Error)?.message,
                onContinue   = ::onContinue,
                onContinueWithoutAccount = ::continueWithoutAccount
            )
        }
    }

    if (showDisclosure) {
        ProminentDisclosureDialog(onAccept = { showDisclosure = false })
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sayfa Layout — Nur-u İslam tasarımı
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NurOnboardingPage(
    imageRes     : Int,
    title        : String,
    description  : String,
    pageIndex    : Int,
    pageCount    : Int,
    isLastPage   : Boolean,
    isLoading    : Boolean,
    errorMessage : String?,
    onContinue   : () -> Unit,
    onContinueWithoutAccount: (() -> Unit)? = null
) {
    // Tüm sayfa koyu yeşil arka plan
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldDeep)
    ) {
        // İnce arabesk doku overlay (yarı saydam)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x1A000000))
        )

        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── HEADER: Logo + Başlık ─────────────────────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cami ikonu daire
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .then(
                            Modifier.clip(CircleShape)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        // Altın çerçeveli daire
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(EmeraldDeep),
                            contentAlignment = Alignment.Center
                        ) {
                            // Border efekti
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text      = "☪",
                                    fontSize  = 22.sp,
                                    color     = Gold
                                )
                            }
                        }
                    }
                }
                Text(
                    text          = "Muslim App",
                    fontSize      = 14.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Gold,
                    letterSpacing = 3.sp
                )
            }

            // ── ORTA: Fotoğraf (arch şekli) ──────────────────────────────────
            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Kemerli (arch) çerçeve — üstü yarım daire, altı düz
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 5f)
                        .clip(ArchShape())
                        .background(BgDarker)
                ) {
                    Image(
                        painter            = painterResource(imageRes),
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .fillMaxSize()
                    )
                    // Alt gradient — fotoğrafı arka planla yumuşakça birleştir
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colorStops = arrayOf(
                                        0.0f to Color.Transparent,
                                        0.6f to Color.Transparent,
                                        1.0f to EmeraldDeep.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    // Altın iç çerçeve
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp)
                            .clip(ArchShape())
                            .background(Color.Transparent)
                    )
                }
                // Dış altın border efekti
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 5f)
                        .clip(ArchShape())
                        .background(Color.Transparent)
                        .then(
                            Modifier.background(
                                Brush.linearGradient(
                                    listOf(GoldDim, Color.Transparent, GoldDim)
                                )
                            )
                        )
                        .padding(1.dp)
                        .clip(ArchShape())
                        .background(Color.Transparent)
                )
            }

            // ── ALT: Başlık + Açıklama + Buton ───────────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 48.dp)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Başlık — altın, serif benzeri büyük harf
                Text(
                    text          = title,
                    fontSize      = 26.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = Gold,
                    textAlign     = TextAlign.Center,
                    letterSpacing = 1.sp,
                    lineHeight    = 34.sp
                )

                Spacer(Modifier.height(10.dp))

                // Açıklama
                Text(
                    text       = description,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Light,
                    color      = GrayText,
                    textAlign  = TextAlign.Center,
                    lineHeight = 22.sp
                )

                // Hata mesajı
                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text      = errorMessage,
                        fontSize  = 12.sp,
                        color     = Color(0xFFFF6B6B),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Buton — altın, tam genişlik
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gold)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = ripple(color = BgDeep),
                            enabled           = !isLoading
                        ) { if (!isLoading) onContinue() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(24.dp),
                            color       = BgDeep,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text       = if (isLastPage) "Google ile Giriş Yap" else "Devam Et",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = BgDeep
                            )
                            if (!isLastPage) {
                                Icon(
                                    imageVector        = Icons.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint               = BgDeep,
                                    modifier           = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (isLastPage && onContinueWithoutAccount != null) {
                    Spacer(Modifier.height(6.dp))
                    TextButton(
                        onClick = { onContinueWithoutAccount() },
                        enabled = !isLoading
                    ) {
                        Text(
                            text       = "Üye olmadan devam et",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color      = GrayText
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Pagination dots
                NurPageIndicator(pageCount = pageCount, currentPage = pageIndex)
            }
        }
    }
}

@Composable
private fun NameOnboardingPage(
    pageIndex: Int,
    pageCount: Int,
    value: String,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onSkip: () -> Unit,
    strings: com.example.islam.core.i18n.AppStrings
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmeraldDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = strings.namePromptTitle,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Gold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = strings.namePromptDescription,
                fontSize = 14.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                label = { Text(strings.nameInputLabel) },
                placeholder = { Text(strings.nameInputPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = GoldDim,
                    focusedLabelColor = Gold,
                    unfocusedLabelColor = GrayText,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = BgDeep
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.nameSave, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onSkip) {
                Text(strings.nameSkip, color = GrayText)
            }
            Spacer(Modifier.height(14.dp))
            NurPageIndicator(pageCount = pageCount, currentPage = pageIndex)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Kemerli (Arch) Shape — üstü yarım daire, altı düz köşeler
// ─────────────────────────────────────────────────────────────────────────────
private class ArchShape : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val path = androidx.compose.ui.graphics.Path().apply {
            val radius = size.width / 2f
            // Üst yarım daire
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.width),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            // Sağ kenar aşağı
            lineTo(size.width, size.height)
            // Alt kenar sola
            lineTo(0f, size.height)
            // Sol kenar yukarı (yarım daireye)
            lineTo(0f, radius)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Pagination Göstergesi — altın rengi
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NurPageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        repeat(pageCount) { idx ->
            val isActive = idx == currentPage
            val width by animateDpAsState(
                targetValue   = if (isActive) 28.dp else 8.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label         = "dot_$idx"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (isActive) Gold else Color(0xFF4B5563)
                    )
            )
        }
    }
}
