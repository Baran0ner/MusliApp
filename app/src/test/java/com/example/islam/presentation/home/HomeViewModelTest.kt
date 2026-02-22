package com.example.islam.presentation.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import app.cash.turbine.test
import com.example.islam.core.util.Resource
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.domain.model.DailyQuote
import com.example.islam.domain.model.UserPreferences
import com.example.islam.domain.usecase.prayer.GetNextPrayerUseCase
import com.example.islam.domain.usecase.prayer.GetPrayerTimesUseCase
import com.example.islam.domain.usecase.quote.GetDailyQuoteUseCase
import com.example.islam.domain.utils.LocationTracker
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var getPrayerTimesUseCase: GetPrayerTimesUseCase
    private lateinit var getNextPrayerUseCase: GetNextPrayerUseCase
    private lateinit var getDailyQuoteUseCase: GetDailyQuoteUseCase
    private lateinit var prefsDataStore: UserPreferencesDataStore
    private lateinit var locationTracker: LocationTracker
    private lateinit var context: Context

    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPrayerTimesUseCase = mockk()
        getNextPrayerUseCase = mockk()
        getDailyQuoteUseCase = mockk()
        prefsDataStore = mockk()
        locationTracker = mockk()
        context = mockk()

        mockkStatic(ContextCompat::class)

        // Mock initialization dependencies
        val mockPrefs = UserPreferences(useGps = false, city = "Istanbul", country = "Turkey")
        coEvery { prefsDataStore.userPreferences } returns flowOf(mockPrefs)
        coEvery { prefsDataStore.prayerStreak } returns flowOf(0)
        every { getDailyQuoteUseCase() } returns DailyQuote("Test", "Test", com.example.islam.domain.model.QuoteType.AYAH)
        
        // Mock permissions denied to prevent auto-start of observePreferences on initialization
        every {
            ContextCompat.checkSelfPermission(any(), any())
        } returns PackageManager.PERMISSION_DENIED
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `loadPrayerTimes emits Loading then Error when offline`() = runTest {
        // Arrange
        // We mock the API layer to return a network error
        coEvery { 
            getPrayerTimesUseCase(any(), any(), any(), any()) 
        } returns Resource.Error("No Internet Connection")

        viewModel = HomeViewModel(
            getPrayerTimesUseCase,
            getNextPrayerUseCase,
            getDailyQuoteUseCase,
            prefsDataStore,
            locationTracker,
            context
        )

        // Act & Assert using Turbine
        viewModel.uiState.test {
            // First emission is the initial state from init{}
            var state = awaitItem()
            assertEquals(false, state.isLoading)
            
            // Trigger a manual refresh imitating a user pull-to-refresh
            viewModel.refresh()
            
            // Second emission: loadPrayerTimes sets isLoading = true
            state = awaitItem()
            assertEquals(true, state.isLoading)

            // Third emission: useCase returns Error, so isLoading = false and error contains message
            state = awaitItem()
            assertEquals(false, state.isLoading)
            assertEquals("No Internet Connection", state.error)

            // Stop listening to Flow
            cancelAndIgnoreRemainingEvents()
        }

        // Must clear ViewModel here inside runTest to terminate infinite loop before runTest attempts to complete
        val clearMethod = androidx.lifecycle.ViewModel::class.java.getDeclaredMethod("clear")
        clearMethod.isAccessible = true
        clearMethod.invoke(viewModel)
    }
}
