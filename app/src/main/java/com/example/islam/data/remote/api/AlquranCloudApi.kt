package com.example.islam.data.remote.api

import com.example.islam.data.remote.dto.SurahDetailResponse
import com.example.islam.data.remote.dto.SurahListResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Alquran.cloud API — surah list, surah verses (Arabic), surah verses (translation), juz verses.
 * Base: https://api.alquran.cloud/
 */
interface AlquranCloudApi {

    @GET("v1/surah")
    suspend fun getSurahList(): SurahListResponse

    /** Arabic verses (uthmani text) */
    @GET("v1/surah/{number}")
    suspend fun getSurah(@Path("number") number: Int): SurahDetailResponse

    /** English translation (Sahih International) */
    @GET("v1/surah/{number}/en.sahih")
    suspend fun getSurahTranslation(@Path("number") number: Int): SurahDetailResponse

    /** Turkish translation (Diyanet İşleri) */
    @GET("v1/surah/{number}/tr.diyanet")
    suspend fun getSurahTranslationTr(@Path("number") number: Int): SurahDetailResponse

    /** Turkish transliteration (okunuş - Çeviriyazı) */
    @GET("v1/surah/{number}/tr.transliteration")
    suspend fun getSurahTransliterationTr(@Path("number") number: Int): SurahDetailResponse

    /** Juz verses (Arabic) */
    @GET("v1/juz/{juzNumber}")
    suspend fun getJuz(@Path("juzNumber") juzNumber: Int): SurahDetailResponse

    /** Juz verses (English translation) */
    @GET("v1/juz/{juzNumber}/en.sahih")
    suspend fun getJuzTranslation(@Path("juzNumber") juzNumber: Int): SurahDetailResponse

    /** Juz verses (Turkish translation - Diyanet) */
    @GET("v1/juz/{juzNumber}/tr.diyanet")
    suspend fun getJuzTranslationTr(@Path("juzNumber") juzNumber: Int): SurahDetailResponse

    /** Juz verses (Turkish transliteration) */
    @GET("v1/juz/{juzNumber}/tr.transliteration")
    suspend fun getJuzTransliterationTr(@Path("juzNumber") juzNumber: Int): SurahDetailResponse

    companion object {
        const val BASE_URL = "https://api.alquran.cloud/"
    }
}
