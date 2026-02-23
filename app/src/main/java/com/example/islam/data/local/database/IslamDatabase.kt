package com.example.islam.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.islam.data.local.dao.DhikrDao
import com.example.islam.data.local.dao.DhikrHistoryDao
import com.example.islam.data.local.dao.IbadahDayDao
import com.example.islam.data.local.dao.PrayerHistoryDao
import com.example.islam.data.local.dao.PrayerTimeDao
import com.example.islam.data.local.entity.DhikrEntity
import com.example.islam.data.local.entity.DhikrHistoryEntity
import com.example.islam.data.local.entity.IbadahDayEntity
import com.example.islam.data.local.entity.PrayerHistoryEntity
import com.example.islam.data.local.entity.PrayerTimeEntity

@Database(
    entities = [
        PrayerTimeEntity::class,
        DhikrEntity::class,
        PrayerHistoryEntity::class,
        DhikrHistoryEntity::class,
        IbadahDayEntity::class
    ],
    version  = 5,
    exportSchema = false
)
abstract class IslamDatabase : RoomDatabase() {
    abstract fun prayerTimeDao()      : PrayerTimeDao
    abstract fun dhikrDao()           : DhikrDao
    abstract fun prayerHistoryDao()   : PrayerHistoryDao
    abstract fun dhikrHistoryDao()    : DhikrHistoryDao
    abstract fun ibadahDayDao()       : IbadahDayDao

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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS dhikr_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        date TEXT NOT NULL,
                        dhikrName TEXT NOT NULL,
                        count INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE prayer_times ADD COLUMN method INTEGER NOT NULL DEFAULT 13")
                db.execSQL("ALTER TABLE prayer_times ADD COLUMN school INTEGER NOT NULL DEFAULT 0")
                db.execSQL("DROP INDEX IF EXISTS index_prayer_times_date_city")
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS index_prayer_times_date_city_country_method_school
                    ON prayer_times(date, city, country, method, school)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS ibadah_day_status (
                        date TEXT NOT NULL PRIMARY KEY,
                        fastDone INTEGER NOT NULL DEFAULT 0,
                        dhikrCount INTEGER NOT NULL DEFAULT 0,
                        quranMinutes INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
