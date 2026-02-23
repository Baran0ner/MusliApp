package com.example.islam.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() {
        baselineProfileRule.collect(
            packageName = TARGET_PACKAGE
        ) {
            pressHome()
            startActivityAndWait()
            exerciseCoreJourneys()
        }
    }

    private fun MacrobenchmarkScope.exerciseCoreJourneys() {
        device.waitForIdle()
        tapAnyLabel("Namaz", "Prayer Times")
        tapAnyLabel("Kıble", "Qibla")
        tapAnyLabel("Zikir", "Dhikr")
        tapAnyLabel("Anasayfa", "Home")
    }

    private fun MacrobenchmarkScope.tapAnyLabel(vararg labels: String) {
        labels.forEach { label ->
            val node = device.findObject(By.text(label)) ?: device.findObject(By.textContains(label))
            if (node != null) {
                node.click()
                device.waitForIdle()
                Thread.sleep(350)
                return
            }
        }
    }

    private companion object {
        const val TARGET_PACKAGE = "com.musliapp.android"
    }
}
