package com.example.islam.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.islam.data.local.dao.DhikrDao
import com.example.islam.data.local.dao.PrayerHistoryDao
import com.example.islam.data.local.dao.PrayerTimeDao
import com.example.islam.data.local.entity.DhikrEntity
import com.example.islam.data.local.entity.PrayerHistoryEntity
import com.example.islam.data.local.entity.PrayerTimeEntity

@Database(
    entities = [PrayerTimeEntity::class, DhikrEntity::class, PrayerHistoryEntity::class],
    version  = 3,
    exportSchema = false
)
abstract class IslamDatabase : RoomDatabase() {
    abstract fun prayerTimeDao()   : PrayerTimeDao
    abstract fun dhikrDao()        : DhikrDao
    abstract fun prayerHistoryDao(): PrayerHistoryDao

    companion object {
        const val DATABASE_NAME = "islam_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE prayer_times ADD COLUMN month INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE prayer_times ADD COLUMN year  INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS prayer_history (
                        date TEXT NOT NULL PRIMARY KEY,
                        isFajrPrayed INTEGER NOT NULL DEFAULT 0,
                        isDhuhrPrayed INTEGER NOT NULL DEFAULT 0,
                        isAsrPrayed INTEGER NOT NULL DEFAULT 0,
                        isMaghribPrayed INTEGER NOT NULL DEFAULT 0,
                        isIshaPrayed INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
