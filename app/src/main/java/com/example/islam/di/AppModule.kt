package com.example.islam.di

import com.example.islam.data.remote.api.AladhanApi
import com.example.islam.data.local.dao.DhikrDao
import com.example.islam.data.local.dao.PrayerHistoryDao
import com.example.islam.data.local.dao.PrayerTimeDao
import com.example.islam.data.local.dao.DhikrHistoryDao
import com.example.islam.data.local.dao.IbadahDayDao
import com.example.islam.data.repository.DhikrHistoryRepositoryImpl
import com.example.islam.data.repository.DhikrRepositoryImpl
import com.example.islam.data.repository.PrayerHistoryRepositoryImpl
import com.example.islam.data.repository.PrayerTimeRepositoryImpl
import com.example.islam.data.repository.RamadanPlannerRepositoryImpl
import com.example.islam.data.repository.SystemTimeTickerRepository
import com.example.islam.domain.repository.DhikrHistoryRepository
import com.example.islam.domain.repository.DhikrRepository
import com.example.islam.domain.repository.PrayerHistoryRepository
import com.example.islam.domain.repository.PrayerTimeRepository
import com.example.islam.domain.repository.RamadanPlannerRepository
import com.example.islam.domain.repository.TimeTickerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePrayerTimeRepository(
        dao: PrayerTimeDao,
        api: AladhanApi
    ): PrayerTimeRepository = PrayerTimeRepositoryImpl(dao, api)

    @Provides
    @Singleton
    fun provideDhikrRepository(
        dao: DhikrDao
    ): DhikrRepository = DhikrRepositoryImpl(dao)

    @Provides
    @Singleton
    fun providePrayerHistoryRepository(
        dao: PrayerHistoryDao
    ): PrayerHistoryRepository = PrayerHistoryRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideDhikrHistoryRepository(
        dao: DhikrHistoryDao
    ): DhikrHistoryRepository = DhikrHistoryRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideRamadanPlannerRepository(
        prayerTimeDao: PrayerTimeDao,
        prayerHistoryDao: PrayerHistoryDao,
        ibadahDayDao: IbadahDayDao
    ): RamadanPlannerRepository = RamadanPlannerRepositoryImpl(
        prayerTimeDao = prayerTimeDao,
        prayerHistoryDao = prayerHistoryDao,
        ibadahDayDao = ibadahDayDao
    )

    @Provides
    @Singleton
    fun provideTimeTickerRepository(
        impl: SystemTimeTickerRepository
    ): TimeTickerRepository = impl
}
