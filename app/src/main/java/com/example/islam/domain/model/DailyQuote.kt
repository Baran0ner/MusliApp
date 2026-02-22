package com.example.islam.domain.model

/** Günlük Ayet veya Hadis öğesinin türünü belirtir. */
enum class QuoteType(val label: String) {
    AYAH("Ayet"),
    HADITH("Hadis")
}

/**
 * Günlük ilham kartında gösterilecek Ayet veya Hadis.
 *
 * @param text   Türkçe meal / Hadis metni
 * @param source Kaynak bilgisi (ör. "Bakara 255", "Buhari, Rikak 18")
 * @param type   [QuoteType.AYAH] veya [QuoteType.HADITH]
 */
data class DailyQuote(
    val text: String,
    val source: String,
    val type: QuoteType
)
