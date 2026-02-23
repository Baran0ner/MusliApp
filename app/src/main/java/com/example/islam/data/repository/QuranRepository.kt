package com.example.islam.data.repository

import com.example.islam.data.remote.api.AlquranCloudApi
import com.example.islam.data.remote.dto.SurahDetailResponse
import com.example.islam.data.remote.dto.SurahListResponse
import com.example.islam.domain.model.JuzListModel
import com.example.islam.domain.model.SurahListModel
import com.example.islam.domain.model.VerseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

private const val API_TIMEOUT_MS = 15_000L
private const val VERSE_API_TIMEOUT_MS = 20_000L

@Singleton
class QuranRepository @Inject constructor(
    private val api: AlquranCloudApi
) {

    /** İnternet/API olmadan anında gösterilecek 114 sure listesi */
    fun getStaticSurahList(): List<SurahListModel> = StaticSurahData.getStaticSurahList()

    suspend fun getSurahList(): Result<List<SurahListModel>> = withContext(Dispatchers.IO) {
        try {
            val response = withTimeout(API_TIMEOUT_MS) { api.getSurahList() }
            if (response.code != 200) return@withContext Result.failure(Exception("API error: ${response.status}"))
            val list = response.data.map { dto ->
                SurahListModel(
                    number = dto.number,
                    name = dto.englishName,
                    englishName = dto.englishName,
                    englishNameTranslation = dto.englishNameTranslation,
                    numberOfAyahs = dto.numberOfAyahs,
                    revelationType = dto.revelationType,
                    arabicName = dto.name,
                    isBookmarked = false
                )
            }
            Result.success(list)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Bağlantı zaman aşımına uğradı. İnternet bağlantınızı kontrol edip tekrar deneyin."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Sureler yüklenemedi. İnternet bağlantınızı kontrol edin."))
        }
    }

    /** 30 Cüz — API'de liste yok, statik */
    fun getJuzList(): List<JuzListModel> =
        (1..30).map { JuzListModel(it, "Cüz $it") }

    suspend fun getSurahVersesWithTranslation(surahNumber: Int): Result<Pair<String, List<VerseModel>>> = withContext(Dispatchers.IO) {
        try {
            val arabicResponse = withTimeout(VERSE_API_TIMEOUT_MS) { api.getSurah(surahNumber) }
            val translationTr = withTimeout(VERSE_API_TIMEOUT_MS) { api.getSurahTranslationTr(surahNumber) }
            val transliterationTr = runCatching { withTimeout(VERSE_API_TIMEOUT_MS) { api.getSurahTransliterationTr(surahNumber) } }.getOrNull()
            if (arabicResponse.code != 200 || translationTr.code != 200)
                return@withContext Result.failure(Exception("API error"))
            val subtitle = translationTr.data.englishNameTranslation
            val arabicAyahs = arabicResponse.data.ayahs
            val translationAyahs = translationTr.data.ayahs.associateBy { it.numberInSurah }
            val transliterationAyahs = transliterationTr?.data?.ayahs?.associateBy { it.numberInSurah } ?: emptyMap()
            val verses = arabicAyahs.map { ayah ->
                VerseModel(
                    numberInSurah = ayah.numberInSurah,
                    arabic = ayah.text.trim(),
                    transliteration = transliterationAyahs[ayah.numberInSurah]?.text?.trim() ?: "",
                    translation = translationAyahs[ayah.numberInSurah]?.text?.trim() ?: "",
                    globalNumber = ayah.number
                )
            }
            Result.success(subtitle to verses)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Bağlantı zaman aşımına uğradı. İnternet bağlantınızı kontrol edip \"Tekrar Dene\" ile yenileyin."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Ayetler yüklenemedi. İnternet bağlantınızı kontrol edin."))
        }
    }

    suspend fun getJuzVerses(juzNumber: Int): Result<Pair<String, List<VerseModel>>> = withContext(Dispatchers.IO) {
        try {
            val response = withTimeout(VERSE_API_TIMEOUT_MS) { api.getJuz(juzNumber) }
            if (response.code != 200) return@withContext Result.failure(Exception("API error"))
            val subtitle = "Cüz $juzNumber"
            val translationTr = runCatching { withTimeout(VERSE_API_TIMEOUT_MS) { api.getJuzTranslationTr(juzNumber) } }.getOrNull()
            val transliterationTr = runCatching { withTimeout(VERSE_API_TIMEOUT_MS) { api.getJuzTransliterationTr(juzNumber) } }.getOrNull()
            val translationMap = translationTr?.data?.ayahs?.associateBy { it.number } ?: emptyMap()
            val transliterationMap = transliterationTr?.data?.ayahs?.associateBy { it.number } ?: emptyMap()
            val verses = response.data.ayahs.map { ayah ->
                VerseModel(
                    numberInSurah = ayah.numberInSurah,
                    arabic = ayah.text.trim(),
                    transliteration = transliterationMap[ayah.number]?.text?.trim() ?: "",
                    translation = translationMap[ayah.number]?.text?.trim() ?: "",
                    globalNumber = ayah.number,
                    surahNumber = ayah.surah?.number
                )
            }
            Result.success(subtitle to verses)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.failure(Exception("Bağlantı zaman aşımına uğradı. İnternet bağlantınızı kontrol edip \"Tekrar Dene\" ile yenileyin."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
