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
    val isBookmarked: Boolean = false
)

/** List item for Cüz tab */
data class JuzListModel(
    val number: Int,
    val displayName: String
)

/** Single verse for reader */
data class VerseModel(
    val numberInSurah: Int,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val globalNumber: Int
)

/** Reader context: surah or juz */
sealed class ReaderType {
    data class Surah(val number: Int, val name: String, val subtitle: String) : ReaderType()
    data class Juz(val number: Int, val displayName: String) : ReaderType()
}
