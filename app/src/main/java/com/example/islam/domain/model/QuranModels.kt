package com.example.islam.domain.model

/** List item for Sure tab */
data class SurahListModel(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val numberOfAyahs: Int,
    val revelationType: String,
    val arabicName: String = "",
    val isBookmarked: Boolean = false,
    val turkishNameTranslation: String? = null,  // Türkçe anlam (gösterimde kullanılır)
    val turkishDisplayName: String? = null      // Türkçe sure adı: "Fatiha Suresi", "Fil Suresi" vb.
)

/** List item for Cüz tab */
data class JuzListModel(
    val number: Int,
    val displayName: String
)

/** Single verse for reader. surahNumber set only for juz (each verse can be from different surah). */
data class VerseModel(
    val numberInSurah: Int,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val globalNumber: Int,
    val surahNumber: Int? = null
)

/** Reader context: surah or juz */
sealed class ReaderType {
    data class Surah(val number: Int, val name: String, val subtitle: String) : ReaderType()
    data class Juz(val number: Int, val displayName: String) : ReaderType()
}
