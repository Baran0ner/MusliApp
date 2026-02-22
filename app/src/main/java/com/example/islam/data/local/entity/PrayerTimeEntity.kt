package com.example.islam.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prayer_times",
    indices = [Index(value = ["date", "city"], unique = true)]
)
data class PrayerTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,        // "19-02-2026"
    val month: Int,          // 1-12  — ay cache kontrolü için
    val year: Int,           // 2026  — ay cache kontrolü için
    val imsak: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val hijriDate: String,
    val city: String,
    val country: String
)
