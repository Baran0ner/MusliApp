package com.example.islam.data.local.dao

import android.content.Context
import app.cash.turbine.test
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.islam.data.local.database.IslamDatabase
import com.example.islam.data.local.entity.PrayerHistoryEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrayerHistoryDaoTest {

    private lateinit var db: IslamDatabase
    private lateinit var dao: PrayerHistoryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, IslamDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.prayerHistoryDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndReadPrayerHistory() = runTest {
        // Arrange
        val entity = PrayerHistoryEntity(
            date = "2026-02-22",
            isFajrPrayed = true,
            isDhuhrPrayed = false,
            isAsrPrayed = true,
            isMaghribPrayed = false,
            isIshaPrayed = true
        )

        // Act
        dao.insertOrUpdate(entity)
        val fetched = dao.getByDate("2026-02-22")

        // Assert
        assertNotNull(fetched)
        assertEquals(true, fetched?.isFajrPrayed)
        assertEquals(false, fetched?.isDhuhrPrayed)
    }

    @Test
    fun getLast7DaysHistory_emitsOrderedUpdates() = runTest {
        // Arrange
        val entity1 = PrayerHistoryEntity(date = "2026-02-20", isFajrPrayed = true, isDhuhrPrayed = true, isAsrPrayed = true, isMaghribPrayed = true, isIshaPrayed = true)
        val entity2 = PrayerHistoryEntity(date = "2026-02-22", isFajrPrayed = false, isDhuhrPrayed = false, isAsrPrayed = false, isMaghribPrayed = false, isIshaPrayed = false)
        val entity3 = PrayerHistoryEntity(date = "2026-02-21", isFajrPrayed = true, isDhuhrPrayed = false, isAsrPrayed = true, isMaghribPrayed = false, isIshaPrayed = true)

        dao.getLast7DaysHistory("2026-02-20", "2026-02-26").test {
            val initial = awaitItem()
            assertTrue(initial.isEmpty())

            dao.insertOrUpdate(entity1)
            dao.insertOrUpdate(entity2)
            dao.insertOrUpdate(entity3)

            var latest = awaitItem()
            while (latest.size < 3) {
                latest = awaitItem()
            }

            assertEquals(3, latest.size)
            assertEquals("2026-02-20", latest[0].date)
            assertEquals("2026-02-21", latest[1].date)
            assertEquals("2026-02-22", latest[2].date)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
