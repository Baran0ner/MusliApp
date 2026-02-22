package com.example.islam.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dhikrs")
data class DhikrEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val arabicText: String,
    val meaning: String,
    val count: Int = 0,
    val targetCount: Int = 33
)
