package com.example.islam.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.islam.data.local.database.IslamDatabase
import com.example.islam.data.local.entity.PrayerHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    fun insertAndReadPrayerHistory() = runBlocking {
        // Arrange
        val entity = PrayerHistoryEntity(
            date = "2026-02-22",
            fajrCompleted = true,
            dhuhrCompleted = false,
            asrCompleted = true,
            maghribCompleted = false,
            ishaCompleted = true
        )

        // Act
        dao.insertOrUpdate(entity)
        val fetched = dao.getByDate("2026-02-22")

        // Assert
        assertNotNull(fetched)
        assertEquals(true, fetched?.fajrCompleted)
        assertEquals(false, fetched?.dhuhrCompleted)
    }

    @Test
    fun getLast7DaysHistory_returnsOrderedList() = runBlocking {
        // Arrange
        val entity1 = PrayerHistoryEntity(date = "2026-02-20", fajrCompleted = true, dhuhrCompleted = true, asrCompleted = true, maghribCompleted = true, ishaCompleted = true)
        val entity2 = PrayerHistoryEntity(date = "2026-02-22", fajrCompleted = false, dhuhrCompleted = false, asrCompleted = false, maghribCompleted = false, ishaCompleted = false)
        val entity3 = PrayerHistoryEntity(date = "2026-02-21", fajrCompleted = true, dhuhrCompleted = false, asrCompleted = true, maghribCompleted = false, ishaCompleted = true)

        dao.insertOrUpdate(entity1)
        dao.insertOrUpdate(entity2)
        dao.insertOrUpdate(entity3)

        // Act
        // Get records from "2026-02-20" onwards. Room will emit a flow, we capture the first emission.
        val historyList = dao.getLast7DaysHistory("2026-02-20").first()

        // Assert
        assertEquals(3, historyList.size)
        // Should be ordered by date ASC
        assertEquals("2026-02-20", historyList[0].date)
        assertEquals("2026-02-21", historyList[1].date)
        assertEquals("2026-02-22", historyList[2].date)
    }
}
