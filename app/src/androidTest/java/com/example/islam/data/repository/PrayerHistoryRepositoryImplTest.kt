package com.example.islam.data.repository

import android.content.Context
import app.cash.turbine.test
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.islam.data.local.database.IslamDatabase
import com.example.islam.domain.model.PrayerType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class PrayerHistoryRepositoryImplTest {

    private lateinit var db: IslamDatabase
    private lateinit var repository: PrayerHistoryRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, IslamDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = PrayerHistoryRepositoryImpl(db.prayerHistoryDao()).apply {
            // 1 Mart 2026 = Pazar, böylece hafta 23 Şubat 2026 Pzt -> 1 Mart 2026 Paz olur.
            currentDateProvider = { LocalDate.of(2026, 3, 1) }
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getLast7Days_emitsWeekOrderedFromMondayToSunday_withDefaults() = runTest {
        repository.getLast7Days().test {
            val days = awaitItem()
            assertEquals(7, days.size)
            assertEquals(
                listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz"),
                days.map { it.shortName }
            )
            assertEquals(
                listOf(
                    "2026-02-23",
                    "2026-02-24",
                    "2026-02-25",
                    "2026-02-26",
                    "2026-02-27",
                    "2026-02-28",
                    "2026-03-01"
                ),
                days.map { it.date }
            )
            assertTrue(days.all { it.history.prayedCount == 0 })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun togglePrayerStatus_whenDayUpdated_flowReflectsChangeImmediately() = runTest {
        val targetDate = "2026-02-27"

        repository.getLast7Days().test {
            awaitItem() // initial

            repository.togglePrayerStatus(targetDate, PrayerType.ASR)

            var updated = awaitItem()
            while (!updated.first { it.date == targetDate }.history.isAsrPrayed) {
                updated = awaitItem()
            }

            val friday = updated.first { it.date == targetDate }
            assertTrue(friday.history.isAsrPrayed)
            assertEquals(1, friday.history.prayedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun togglePrayerStatus_concurrentWrites_producesDeterministicParity() = runTest {
        val targetDate = "2026-02-27"

        val tasks = buildList {
            repeat(31) {
                add(async(Dispatchers.Default) { repository.togglePrayerStatus(targetDate, PrayerType.FAJR) })
            }
            repeat(30) {
                add(async(Dispatchers.Default) { repository.togglePrayerStatus(targetDate, PrayerType.DHUHR) })
            }
        }
        tasks.awaitAll()

        val day = repository.getLast7Days().first().first { it.date == targetDate }
        // 31 toggle => true (tek), 30 toggle => false (çift)
        assertTrue(day.history.isFajrPrayed)
        assertFalse(day.history.isDhuhrPrayed)
    }
}
