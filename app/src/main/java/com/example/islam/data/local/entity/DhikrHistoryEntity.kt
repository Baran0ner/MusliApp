package com.example.islam.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dhikr_history")
data class DhikrHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,       // yyyy-MM-dd
    val dhikrName: String,
    val count: Int
)
