package com.example.islam.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.presentation.dhikr.DhikrScreen
import com.example.islam.presentation.home.HomeScreen
import com.example.islam.presentation.onboarding.OnboardingScreen
import com.example.islam.presentation.prayer.PrayerScreen
import com.example.islam.presentation.qibla.QiblaScreen
import com.example.islam.presentation.quran.QuranScreen
import com.example.islam.presentation.quran.SurahReaderScreen
import com.example.islam.presentation.auth.GoogleAuthScreen
import com.example.islam.presentation.settings.SettingsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    prefsDataStore: UserPreferencesDataStore
) {
    // Onboarding tamamlandı mı? null = henüz DataStore'dan okumadık (splash bekleme durumu)
    val onboardingDone by prefsDataStore.onboardingCompleted.collectAsState(initial = null)

    // DataStore henüz yanıt vermediyse boş kutu göster (genellikle <1 frame)
    if (onboardingDone == null) {
        Box(modifier = Modifier.fillMaxSize())
        return
    }

    val startDestination = if (onboardingDone == true) Screen.Home.route
                           else Screen.Onboarding.route

    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Quran.route) {
            QuranScreen(navController = navController)
        }
        composable(
            route = Screen.SurahReader.route,
            arguments = listOf(
                navArgument("surahId") { type = NavType.StringType; defaultValue = "67" },
                navArgument("surahName") { type = NavType.StringType; defaultValue = "Al-Mulk" },
                navArgument("isJuz") { type = NavType.StringType; defaultValue = "false" },
                navArgument("startVerse") { type = NavType.StringType; defaultValue = "0" }
            )
        ) { backStackEntry ->
            val surahId = backStackEntry.arguments?.getString("surahId") ?: "67"
            val surahName = backStackEntry.arguments?.getString("surahName") ?: "Al-Mulk"
            val startVerse = backStackEntry.arguments?.getString("startVerse")?.toIntOrNull() ?: 0
            SurahReaderScreen(
                navController = navController,
                surahId = surahId,
                surahName = surahName,
                isJuz = backStackEntry.arguments?.getString("isJuz") == "true",
                startVerse = startVerse
            )
        }
        composable(Screen.PrayerTimes.route) {
            PrayerScreen(navController = navController)
        }
        composable(Screen.Dhikr.route) {
            DhikrScreen()
        }
        composable(Screen.Qibla.route) {
            QiblaScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.GoogleAuth.route) {
            GoogleAuthScreen(navController = navController)
        }
    }
}
