package com.example.islam.di

import android.content.Context
import androidx.room.Room
import com.example.islam.data.local.dao.DhikrDao
import com.example.islam.data.local.dao.DhikrHistoryDao
import com.example.islam.data.local.dao.IbadahDayDao
import com.example.islam.data.local.dao.PrayerHistoryDao
import com.example.islam.data.local.dao.PrayerTimeDao
import com.example.islam.data.local.database.IslamDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): IslamDatabase =
        Room.databaseBuilder(
            context,
            IslamDatabase::class.java,
            IslamDatabase.DATABASE_NAME
        )
            .addMigrations(
                IslamDatabase.MIGRATION_1_2,
                IslamDatabase.MIGRATION_2_3,
                IslamDatabase.MIGRATION_3_4,
                IslamDatabase.MIGRATION_4_5
            )
            .build()

    @Provides
    fun providePrayerTimeDao(db: IslamDatabase): PrayerTimeDao = db.prayerTimeDao()

    @Provides
    fun provideDhikrDao(db: IslamDatabase): DhikrDao = db.dhikrDao()

    @Provides
    fun providePrayerHistoryDao(db: IslamDatabase): PrayerHistoryDao = db.prayerHistoryDao()

    @Provides
    fun provideDhikrHistoryDao(db: IslamDatabase): DhikrHistoryDao = db.dhikrHistoryDao()

    @Provides
    fun provideIbadahDayDao(db: IslamDatabase): IbadahDayDao = db.ibadahDayDao()
}
