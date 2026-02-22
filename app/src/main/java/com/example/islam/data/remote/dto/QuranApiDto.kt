package com.example.islam.data.remote.dto

import com.google.gson.annotations.SerializedName

// ─────────────────────────────────────────────────────────────────────────────
// Alquran.cloud API DTOs
// ─────────────────────────────────────────────────────────────────────────────

data class SurahListResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: List<SurahDto>
)

data class SurahDto(
    @SerializedName("number") val number: Int,
    @SerializedName("name") val name: String,
    @SerializedName("englishName") val englishName: String,
    @SerializedName("englishNameTranslation") val englishNameTranslation: String,
    @SerializedName("numberOfAyahs") val numberOfAyahs: Int,
    @SerializedName("revelationType") val revelationType: String
)

data class SurahDetailResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: SurahDetailDataDto
)

data class SurahDetailDataDto(
    @SerializedName("number") val number: Int,
    @SerializedName("name") val name: String,
    @SerializedName("englishName") val englishName: String,
    @SerializedName("englishNameTranslation") val englishNameTranslation: String,
    @SerializedName("numberOfAyahs") val numberOfAyahs: Int,
    @SerializedName("revelationType") val revelationType: String,
    @SerializedName("ayahs") val ayahs: List<AyahDto>
)

data class AyahDto(
    @SerializedName("number") val number: Int,
    @SerializedName("text") val text: String,
    @SerializedName("numberInSurah") val numberInSurah: Int,
    @SerializedName("juz") val juz: Int?,
    @SerializedName("manzil") val manzil: Int?,
    @SerializedName("page") val page: Int?,
    @SerializedName("ruku") val ruku: Int?,
    @SerializedName("hizbQuarter") val hizbQuarter: Int?,
    @SerializedName("sajda") val sajda: Boolean?
)
