package com.example.islam.di

import com.example.islam.BuildConfig
import com.example.islam.data.remote.api.AladhanApi
import com.example.islam.data.remote.api.AlquranCloudApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(AladhanApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAladhanApi(retrofit: Retrofit): AladhanApi =
        retrofit.create(AladhanApi::class.java)

    @Provides
    @Singleton
    @AlquranRetrofit
    fun provideAlquranRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(AlquranCloudApi.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideAlquranCloudApi(@AlquranRetrofit retrofit: Retrofit): AlquranCloudApi =
        retrofit.create(AlquranCloudApi::class.java)
}
