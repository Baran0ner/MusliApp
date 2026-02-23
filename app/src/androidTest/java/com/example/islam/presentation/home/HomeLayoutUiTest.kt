package com.example.islam.presentation.home

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeLayoutUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dawnHomeScreen_smallViewport_isVerticallyScrollable() {
        composeRule.setContent {
            Box(modifier = Modifier.size(width = 280.dp, height = 460.dp)) {
                DawnHomeScreen(
                    verseText = "Uzun ayet metni satır kaydırmayı ve dikey taşma durumunu doğrulamak için özellikle uzatılmıştır. " +
                            "Bu metin, ekran yüksekliği dar olduğunda içeriğin scroll ile güvenli kalmasını test eder.",
                    verseRef = "Test 1"
                )
            }
        }

        composeRule.onNodeWithTag("home_main_scroll")
            .assertIsDisplayed()
            .performTouchInput { swipeUp() }

        composeRule.onNodeWithTag("home_prayer_chips_row")
            .assertIsDisplayed()
            .performTouchInput { swipeLeft() }
    }

    @Test
    fun streakCard_compactWidth_usesHorizontalScrollableDayRow() {
        composeRule.setContent {
            Box(modifier = Modifier.size(width = 280.dp, height = 300.dp)) {
                DawnHomeScreen(
                    streakDays = 4,
                    completedToday = 2,
                    dailyGoal = 5,
                    weeklyVisitMask = 0b0011111
                )
            }
        }

        composeRule.onNodeWithTag("streak_day_row")
            .assertIsDisplayed()
            .assert(
                SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange)
            )
    }
}
