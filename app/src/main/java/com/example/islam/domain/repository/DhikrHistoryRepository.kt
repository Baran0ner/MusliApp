package com.example.islam.domain.repository

import com.example.islam.domain.model.DhikrDay
import com.example.islam.domain.model.DhikrRecord
import kotlinx.coroutines.flow.Flow

interface DhikrHistoryRepository {
    suspend fun saveRecord(date: String, dhikrName: String, count: Int)
    fun getLast7Days(): Flow<List<DhikrDay>>
    suspend fun getRecordsByDate(date: String): List<DhikrRecord>
}
