package com.example.islam.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────────────────────
// Açık Tema — Ant Design Mobile ilham alınarak: Saf beyaz arka plan,
// ADM-box kart rengi, ince ADM-border ayırıcılar, İslami yeşil primary
// ─────────────────────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    // Birincil — İslami Yeşil
    primary             = IslamicGreen40,
    onPrimary           = White,
    primaryContainer    = IslamicGreen90,
    onPrimaryContainer  = IslamicGreen10,

    // İkincil — Altın
    secondary           = Gold40,
    onSecondary         = White,
    secondaryContainer  = Gold90,
    onSecondaryContainer= Gold10,

    // Üçüncül — Firuze
    tertiary            = Teal40,
    onTertiary          = White,
    tertiaryContainer   = Teal90,
    onTertiaryContainer = Teal10,

    // ADM: saf beyaz arka plan — net, temiz
    background          = AdmBackground,
    onBackground        = AdmText,

    // ADM: yüzey = beyaz, kart arka planı = hafif gri (#F5F5F5)
    surface                  = AdmBackground,
    onSurface                = AdmText,
    surfaceVariant           = AdmBox,
    onSurfaceVariant         = AdmTextSecondary,
    surfaceContainer         = AdmBox,          // kart arka planı
    surfaceContainerLow      = AdmBackground,
    surfaceContainerHigh     = AdmBorder,
    surfaceContainerHighest  = Color(0xFFE8E8E8),

    // ADM: ince, saf kenarlıklar
    outline             = AdmTextLight,         // #CCCCCC — ince border
    outlineVariant      = AdmBorder,            // #EEEEEE — ayırıcı

    // Hata
    error               = Error40,
    onError             = White,
    errorContainer      = Error90,
    onErrorContainer    = Error10,

    // Özel yüzeyler
    inverseSurface      = Neutral20,
    inverseOnSurface    = Neutral95,
    inversePrimary      = IslamicGreen80,

    scrim               = Neutral10
)

// ─────────────────────────────────────────────────────────────────────────────
// Koyu Tema — Koyu Zümrüt Yeşili arka plan, Altın vurgu
// ─────────────────────────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary             = IslamicGreen80,
    onPrimary           = IslamicGreen20,
    primaryContainer    = IslamicGreen30,
    onPrimaryContainer  = IslamicGreen90,

    secondary           = Gold80,
    onSecondary         = Gold20,
    secondaryContainer  = Gold30,
    onSecondaryContainer= Gold90,

    tertiary            = Teal80,
    onTertiary          = Teal20,
    tertiaryContainer   = Teal20,
    onTertiaryContainer = Teal90,

    // Koyu Zümrüt arka plan
    background          = EmeraldBg,
    onBackground        = Neutral90,

    surface                  = EmeraldSurface,
    onSurface                = Neutral90,
    surfaceVariant           = EmeraldCard,
    onSurfaceVariant         = NeutralVariant80,
    surfaceContainer         = EmeraldCard,        // kart arka planı
    surfaceContainerLow      = EmeraldBg,
    surfaceContainerHigh     = EmeraldCardHigh,
    surfaceContainerHighest  = EmeraldCardBorder,

    outline             = EmeraldCardBorder,
    outlineVariant      = EmeraldCard,

    error               = Error80,
    onError             = Error20,
    errorContainer      = Error40,
    onErrorContainer    = Error90,

    inverseSurface      = Neutral90,
    inverseOnSurface    = Neutral20,
    inversePrimary      = IslamicGreen40,

    scrim               = Neutral10
)

// ─────────────────────────────────────────────────────────────────────────────
// IslamTheme
// appTheme: 0 = Sistem Varsayılanı, 1 = Koyu Tema, 2 = Açık Tema
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun IslamTheme(
    appTheme: Int = 0,
    darkTheme: Boolean = isSystemInDarkTheme(), // eski API — sadece appTheme=0 durumunda kullanılır
    content: @Composable () -> Unit
) {
    val useDark = when (appTheme) {
        1    -> true
        2    -> false
        else -> darkTheme // sistem varsayılanı
    }
    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = IslamicTypography,
        content     = content
    )
}
