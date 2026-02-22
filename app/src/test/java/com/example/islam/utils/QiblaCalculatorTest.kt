package com.example.islam.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class QiblaCalculatorTest {

    // Test coordinates and expected Qibla angles (approximate)
    // Istanbul: 41.0082, 28.9784 -> ~153.3 degrees
    // London: 51.5072, -0.1276 -> ~119.0 degrees
    // Tokyo: 35.6895, 139.6917 -> ~293.0 degrees

    @Test
    fun `calculateQiblaDirection from Istanbul returns correct angle`() {
        val angle = QiblaCalculator.calculateQiblaDirection(41.0082, 28.9784)
        // Istanbul to Mecca is approx 153.3 degrees
        assertEquals(153.3f, angle, 0.5f)
    }

    @Test
    fun `calculateQiblaDirection from London returns correct angle`() {
        val angle = QiblaCalculator.calculateQiblaDirection(51.5072, -0.1276)
        // London to Mecca is approx 119.0 degrees
        assertEquals(119.0f, angle, 0.5f)
    }

    @Test
    fun `calculateQiblaDirection from Tokyo returns correct angle`() {
        val angle = QiblaCalculator.calculateQiblaDirection(35.6895, 139.6917)
        // Tokyo to Mecca is approx 293.0 degrees
        assertEquals(293.0f, angle, 0.5f)
    }
}
