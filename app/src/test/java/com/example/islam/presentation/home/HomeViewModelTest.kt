package com.example.islam.presentation.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.islam.core.util.Resource
import com.example.islam.data.datastore.UserPreferencesDataStore
import com.example.islam.domain.model.DailyQuote
import com.example.islam.domain.model.UserPreferences
import com.example.islam.domain.repository.PrayerHistoryRepository
import com.example.islam.domain.repository.TimeTickerRepository
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
import kotlinx.coroutines.test.advanceUntilIdle
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
    private lateinit var prayerHistoryRepository: PrayerHistoryRepository
    private lateinit var prefsDataStore: UserPreferencesDataStore
    private lateinit var timeTickerRepository: TimeTickerRepository
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
        prayerHistoryRepository = mockk()
        prefsDataStore = mockk()
        timeTickerRepository = mockk()
        locationTracker = mockk()
        context = mockk()

        mockkStatic(ContextCompat::class)

        // Mock initialization dependencies
        val mockPrefs = UserPreferences(useGps = false, city = "Istanbul", country = "Turkey")
        every { prefsDataStore.userPreferences } returns flowOf(mockPrefs)
        every { prefsDataStore.onboardingCompleted } returns flowOf(true)
        every { prefsDataStore.prayerStreak } returns flowOf(0)
        every { prefsDataStore.completedPrayersToday } returns flowOf(emptySet())
        coEvery { prefsDataStore.ensureStreakUpToDate() } returns Unit
        every { prayerHistoryRepository.getLast7Days() } returns flowOf(emptyList())
        every { timeTickerRepository.secondTicker() } returns flowOf(0L)
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
    fun `loadPrayerTimes sets error when offline`() = runTest {
        coEvery { 
            getPrayerTimesUseCase(any(), any(), any(), any()) 
        } returns Resource.Error("No Internet Connection")

        viewModel = HomeViewModel(
            getPrayerTimesUseCase,
            getNextPrayerUseCase,
            getDailyQuoteUseCase,
            prayerHistoryRepository,
            prefsDataStore,
            timeTickerRepository,
            locationTracker,
            context
        )

        assertEquals(false, viewModel.uiState.value.isLoading)
        viewModel.refresh()
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("No Internet Connection", state.error)

    }
}
