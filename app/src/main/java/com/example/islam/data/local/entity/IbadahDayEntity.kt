package com.example.islam.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ibadah_day_status")
data class IbadahDayEntity(
    @PrimaryKey val date: String, // yyyy-MM-dd
    val fastDone: Boolean = false,
    val dhikrCount: Int = 0,
    val quranMinutes: Int = 0
)
