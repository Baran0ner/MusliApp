package com.example.islam.di

import android.content.Context
import com.example.islam.data.location.DefaultLocationTracker
import com.example.islam.domain.utils.LocationTracker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Abstract modül: @Binds ve @Provides aynı modülde kullanılabilmesi için
 * abstract class + companion object pattern'i uygulanır.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    /** [LocationTracker] → [DefaultLocationTracker] bağlaması */
    @Binds
    @Singleton
    abstract fun bindLocationTracker(
        impl: DefaultLocationTracker
    ): LocationTracker

    companion object {

        /** Tüm location işlemlerinde kullanılan FusedLocationProviderClient */
        @Provides
        @Singleton
        fun provideFusedLocationClient(
            @ApplicationContext context: Context
        ): FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
    }
}
