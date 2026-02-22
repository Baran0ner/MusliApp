package com.example.islam.domain.usecase.quote

import com.example.islam.data.local.DailyQuoteDataSource
import com.example.islam.domain.model.DailyQuote
import java.util.Calendar
import javax.inject.Inject

/**
 * Günün tarihini baz alarak listeden deterministik biçimde bir [DailyQuote] seçer.
 *
 * Seçim: `dayOfYear % quotes.size`
 * - Aynı gün içinde her çağrıda aynı öğeyi döndürür.
 * - Gece yarısında liste otomatik olarak bir sonraki öğeye geçer.
 * - Liste boyutu değişirse sıra kayar ama tutarlılık bozulmaz.
 */
class GetDailyQuoteUseCase @Inject constructor(
    private val dataSource: DailyQuoteDataSource
) {
    operator fun invoke(): DailyQuote {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val quotes    = dataSource.quotes
        return quotes[dayOfYear % quotes.size]
    }
}
