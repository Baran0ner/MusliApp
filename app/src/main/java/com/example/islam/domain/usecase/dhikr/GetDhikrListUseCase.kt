package com.example.islam.domain.usecase.dhikr

import com.example.islam.domain.model.Dhikr
import com.example.islam.domain.repository.DhikrRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDhikrListUseCase @Inject constructor(
    private val repository: DhikrRepository
) {
    operator fun invoke(): Flow<List<Dhikr>> = repository.getAllDhikrs()
}
