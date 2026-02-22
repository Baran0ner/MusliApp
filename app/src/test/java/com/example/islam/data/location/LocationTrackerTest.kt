package com.example.islam.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LocationTrackerTest {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var context: Context
    private lateinit var defaultLocationTracker: DefaultLocationTracker

    @Before
    fun setup() {
        fusedLocationClient = mockk()
        context = mockk()
        defaultLocationTracker = DefaultLocationTracker(fusedLocationClient, context)
        mockkStatic(ContextCompat::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getCurrentLocation returns null when permissions are denied`() = runBlocking {
        // Arrange: Mock ContextCompat to return PERMISSION_DENIED for both Location permissions
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED

        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED

        // Act: Request location
        val result = defaultLocationTracker.getCurrentLocation()

        // Assert: System should early exit and return null without crashing
        assertNull("Location should be safely null when permissions are missing/denied", result)
    }
}
