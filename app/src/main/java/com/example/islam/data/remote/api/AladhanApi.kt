package com.example.islam.data.remote.api

import com.example.islam.data.remote.dto.PrayerCalendarResponse
import com.example.islam.data.remote.dto.PrayerTimeResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AladhanApi {

    /**
     * Get prayer times for a single day by city name.
     * Example: GET /v1/timingsByCity?city=Istanbul&country=Turkey&method=13&school=1
     */
    @GET("v1/timingsByCity")
    suspend fun getPrayerTimesByCity(
        @Query("city")    city: String,
        @Query("country") country: String,
        @Query("method")  method: Int = 13,
        @Query("school")  school: Int = 0,   // 0=Şafii, 1=Hanefi
        @Query("date")    date: String? = null
    ): PrayerTimeResponse

    /**
     * Get prayer times for a single day by GPS coordinates.
     * Example: GET /v1/timings/1740009600?latitude=41.0082&longitude=28.9784&method=13&school=1
     */
    @GET("v1/timings/{timestamp}")
    suspend fun getPrayerTimesByCoordinates(
        @Path("timestamp")   timestamp: Long,
        @Query("latitude")   latitude: Double,
        @Query("longitude")  longitude: Double,
        @Query("method")     method: Int = 13,
        @Query("school")     school: Int = 0
    ): PrayerTimeResponse

    /**
     * Get prayer times for an entire month by city name.
     * Returns a list with one entry per day (28-31 items).
     * Example: GET /v1/calendarByCity?city=Istanbul&country=Turkey&method=13&school=1&month=2&year=2026
     */
    @GET("v1/calendarByCity")
    suspend fun getPrayerCalendarByCity(
        @Query("city")    city: String,
        @Query("country") country: String,
        @Query("method")  method: Int = 13,
        @Query("school")  school: Int = 0,
        @Query("month")   month: Int,
        @Query("year")    year: Int
    ): PrayerCalendarResponse

    companion object {
        const val BASE_URL = "https://api.aladhan.com/"
    }
}
