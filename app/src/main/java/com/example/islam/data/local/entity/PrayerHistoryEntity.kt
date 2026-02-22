package com.example.islam.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_history")
data class PrayerHistoryEntity(
    @PrimaryKey val date         : String,   // "yyyy-MM-dd"
    val isFajrPrayed             : Boolean = false,
    val isDhuhrPrayed            : Boolean = false,
    val isAsrPrayed              : Boolean = false,
    val isMaghribPrayed          : Boolean = false,
    val isIshaPrayed             : Boolean = false
)
