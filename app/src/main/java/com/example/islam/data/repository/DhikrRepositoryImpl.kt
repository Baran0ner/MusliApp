package com.example.islam.data.repository

import com.example.islam.data.local.dao.DhikrDao
import com.example.islam.data.mapper.toDomain
import com.example.islam.data.mapper.toEntity
import com.example.islam.domain.model.Dhikr
import com.example.islam.domain.model.defaultDhikrList
import com.example.islam.domain.repository.DhikrRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DhikrRepositoryImpl @Inject constructor(
    private val dao: DhikrDao
) : DhikrRepository {

    override fun getAllDhikrs(): Flow<List<Dhikr>> =
        dao.getAllDhikrs().map { list -> list.map { it.toDomain() } }

    override suspend fun insertDhikr(dhikr: Dhikr) {
        dao.insertDhikr(dhikr.toEntity())
    }

    override suspend fun updateDhikr(dhikr: Dhikr) {
        dao.updateDhikr(dhikr.toEntity())
    }

    override suspend fun deleteDhikr(dhikr: Dhikr) {
        dao.deleteDhikr(dhikr.toEntity())
    }

    override suspend fun incrementCount(id: Long) = dao.incrementCount(id)

    override suspend fun resetCount(id: Long) = dao.resetCount(id)

    override suspend fun resetAllCounts() = dao.resetAllCounts()

    /** Seeds default dhikrs on first run; call from ViewModel.init */
    override suspend fun seedIfEmpty() {
        if (dao.getCount() == 0) {
            defaultDhikrList.forEach { dao.insertDhikr(it.toEntity()) }
        }
    }
}
