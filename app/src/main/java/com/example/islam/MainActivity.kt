package com.example.islam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.islam.core.i18n.LocalStrings
import com.example.islam.core.i18n.AppStrings
import com.example.islam.core.i18n.stringsFor
import com.example.islam.core.navigation.NavGraph
import com.example.islam.core.navigation.Screen
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.notification.NotificationHelper
import com.example.islam.presentation.settings.SettingsViewModel
import com.example.islam.ui.theme.IslamTheme
import com.example.islam.worker.PrayerTimeUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

// ─── Bottom nav veri modeli ───────────────────────────────────────────────────

data class BottomNavItem(
    val screen   : Screen,
    val labelFn  : (AppStrings) -> String,
    val icon     : ImageVector? = null,
    val iconResId: Int? = null
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home,        { it.navHome },        iconResId = R.drawable.icon_anasayfa),
    BottomNavItem(Screen.Quran,       { it.navQuran },       iconResId = R.drawable.icon_kuran),
    BottomNavItem(Screen.PrayerTimes, { it.navPrayerTimes }, iconResId = R.drawable.icon_namaz),
    BottomNavItem(Screen.Dhikr,       { it.navDhikr },       iconResId = R.drawable.icon_tespih),
    BottomNavItem(Screen.Qibla,       { it.navQibla },       iconResId = R.drawable.icon_kible),
    BottomNavItem(Screen.Settings,    { it.navSettings },    iconResId = R.drawable.icon_ayarlar)
)

// ─── Activity ─────────────────────────────────────────────────────────────────

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefsDataStore: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannels(this)
        PrayerTimeUpdateWorker.enqueueDailyWork(this)
        setContent { IslamApp(prefsDataStore = prefsDataStore) }
    }
}

// ─── Uygulama kökü ────────────────────────────────────────────────────────────

@Composable
fun IslamApp(prefsDataStore: UserPreferencesDataStore) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val prefs = settingsState.preferences
    val strings = stringsFor(prefs.language)
    val layoutDirection = if (prefs.language == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(
        LocalStrings provides strings,
        LocalLayoutDirection provides layoutDirection
    ) {
        IslamTheme(appTheme = prefs.appTheme) {
            val navController  = rememberNavController()
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute   = backStackEntry?.destination?.route

            val showBottomBar = currentRoute != Screen.Onboarding.route

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        IslamBottomNavBar(navController, strings)
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    NavGraph(
                        navController  = navController,
                        prefsDataStore = prefsDataStore
                    )
                }
            }
        }
    }
}

// ─── Animasyonlu Alt Navigasyon Çubuğu ───────────────────────────────────────

@Composable
private fun IslamBottomNavBar(navController: NavController, strings: AppStrings) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    // Referans: altın = seçili, gray-400 = seçili değil
    val activeColor   = Color(0xFFD4AF37)  // primary gold
    val inactiveColor = Color(0xFF9CA3AF)  // gray-400

    Surface(
        color           = Color(0xFF0B2419).copy(alpha = 0.95f),  // background-dark/95
        tonalElevation  = 0.dp,
        shadowElevation = 0.dp,
        modifier        = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
            Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            bottomNavItems.forEach { item ->
                val isSelected = currentRoute == item.screen.route
                NavMenuItem(
                    item          = item,
                    label         = item.labelFn(strings),
                    isSelected    = isSelected,
                    activeColor   = activeColor,
                    inactiveColor = inactiveColor,
                    onClick       = {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
        }
        }
    }
}

// ─── Tek Nav Öğesi ────────────────────────────────────────────────────────────

@Composable
private fun NavMenuItem(
    item         : BottomNavItem,
    label        : String,
    isSelected   : Boolean,
    activeColor  : Color,
    inactiveColor: Color,
    onClick      : () -> Unit
) {
    // Bounce animasyonu — React iconBounce keyframes
    val bounceAnim = remember { Animatable(0f) }
    LaunchedEffect(isSelected) {
        if (isSelected) {
            bounceAnim.snapTo(0f)
            bounceAnim.animateTo(
                targetValue   = 0f,
                animationSpec = keyframes {
                    durationMillis = 480
                    -7f at 100 using FastOutSlowInEasing
                     0f at 220 using LinearEasing
                    -3f at 320 using LinearEasing
                     0f at 420 using LinearEasing
                }
            )
        }
    }

    // Etiket rengi — seçili: altın, değil: soluk beyaz (hepsi görünür)
    val labelColor by animateColorAsState(
        targetValue   = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 220),
        label         = "labelColor_${item.screen.route}"
    )

    // Referans tasarımda alt çizgi yok
    val underlineWidth = 0.dp

    // Renk animasyonu
    val iconColor by animateColorAsState(
        targetValue   = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 180),
        label         = "color_${item.screen.route}"
    )

    // İkon boyutu — aktif item'da hafif büyür
    val iconSize by animateDpAsState(
        targetValue   = if (isSelected) 28.dp else 26.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "iconSize_${item.screen.route}"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null
            ) { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .widthIn(min = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        // İkon — bounce graphicsLayer ile
        Box(
            modifier         = Modifier.size(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (item.iconResId != null) {
                androidx.compose.ui.layout.Layout(
                    content = {
                        Image(
                            painter            = painterResource(item.iconResId),
                            contentDescription = label,
                            modifier           = Modifier.size(iconSize)
                        )
                    }
                ) { measurables, constraints ->
                    val placeable = measurables.first().measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, bounceAnim.value.toInt())
                    }
                }
            } else {
                androidx.compose.ui.layout.Layout(
                    content = {
                        Icon(
                            imageVector        = item.icon!!,
                            contentDescription = label,
                            tint               = iconColor,
                            modifier           = Modifier.size(iconSize)
                        )
                    }
                ) { measurables, constraints ->
                    val placeable = measurables.first().measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, bounceAnim.value.toInt())
                    }
                }
            }
        }

        // Etiket — tüm öğelerde görünür (Kuran, Anasayfa, Namaz, ...)
        Text(
            text       = label,
            fontSize   = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color      = labelColor,
            maxLines   = 1
        )

    }
}
