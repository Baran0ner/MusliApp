package com.example.islam.domain.usecase.dhikr

import com.example.islam.domain.repository.DhikrRepository
import javax.inject.Inject

class IncrementDhikrUseCase @Inject constructor(
    private val repository: DhikrRepository
) {
    suspend operator fun invoke(id: Long) = repository.incrementCount(id)
}
