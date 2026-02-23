package com.example.islam.domain.repository

import kotlinx.coroutines.flow.Flow

interface TimeTickerRepository {
    fun secondTicker(): Flow<Long>
}
