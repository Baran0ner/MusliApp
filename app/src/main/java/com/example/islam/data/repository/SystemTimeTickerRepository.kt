package com.example.islam.data.repository

import com.example.islam.domain.repository.TimeTickerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemTimeTickerRepository @Inject constructor() : TimeTickerRepository {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val ticker = flow {
        while (currentCoroutineContext().isActive) {
            emit(System.currentTimeMillis())
            val wait = 1_000L - (System.currentTimeMillis() % 1_000L)
            delay(if (wait in 1..1_000) wait else 1_000L)
        }
    }.shareIn(
        scope = appScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000L),
        replay = 1
    )

    override fun secondTicker(): Flow<Long> = ticker
}
