package com.example.islam.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class QiblaCalculatorTest {

    // Test coordinates and expected great-circle bearings (true north reference).
    // Istanbul: 41.0082, 28.9784 -> ~151.62°
    // London:   51.5072, -0.1276 -> ~118.99°
    // Tokyo:    35.6895, 139.6917 -> ~293.02°

    @Test
    fun `calculateQiblaDirection from Istanbul returns correct angle`() {
        val angle = QiblaCalculator.calculateQiblaDirection(41.0082, 28.9784)
        assertEquals(151.62f, angle, 0.5f)
    }

    @Test
    fun `calculateQiblaDirection from London returns correct angle`() {
        val angle = QiblaCalculator.calculateQiblaDirection(51.5072, -0.1276)
        assertEquals(118.99f, angle, 0.5f)
    }

    @Test
    fun `calculateQiblaDirection from Tokyo returns correct angle`() {
        val angle = QiblaCalculator.calculateQiblaDirection(35.6895, 139.6917)
        assertEquals(293.02f, angle, 0.5f)
    }
}
