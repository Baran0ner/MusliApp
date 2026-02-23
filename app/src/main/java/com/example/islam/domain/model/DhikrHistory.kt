package com.example.islam.domain.model

data class DhikrRecord(
    val dhikrName: String,
    val count: Int
)

data class DhikrDay(
    val date: String,       // yyyy-MM-dd
    val shortName: String,  // Pzt, Sal ...
    val records: List<DhikrRecord>
) {
    val totalCount: Int get() = records.sumOf { it.count }
}
