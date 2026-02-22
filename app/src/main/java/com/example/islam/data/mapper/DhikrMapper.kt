package com.example.islam.data.mapper

import com.example.islam.data.local.entity.DhikrEntity
import com.example.islam.domain.model.Dhikr

fun DhikrEntity.toDomain() = Dhikr(
    id = id,
    name = name,
    arabicText = arabicText,
    meaning = meaning,
    count = count,
    targetCount = targetCount
)

fun Dhikr.toEntity() = DhikrEntity(
    id = id,
    name = name,
    arabicText = arabicText,
    meaning = meaning,
    count = count,
    targetCount = targetCount
)
