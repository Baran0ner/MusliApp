package com.example.islam.domain.repository

import com.example.islam.domain.model.Dhikr
import kotlinx.coroutines.flow.Flow

interface DhikrRepository {
    fun getAllDhikrs(): Flow<List<Dhikr>>
    suspend fun insertDhikr(dhikr: Dhikr)
    suspend fun updateDhikr(dhikr: Dhikr)
    suspend fun deleteDhikr(dhikr: Dhikr)
    suspend fun incrementCount(id: Long)
    suspend fun resetCount(id: Long)
    suspend fun resetAllCounts()
    suspend fun seedIfEmpty()
}
