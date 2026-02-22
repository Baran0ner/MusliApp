package com.example.islam.data.repository

import com.example.islam.core.util.DateUtil
import com.example.islam.core.util.Resource
import com.example.islam.data.local.dao.PrayerTimeDao
import com.example.islam.data.mapper.toEntity
import com.example.islam.data.mapper.toDomain
import com.example.islam.data.remote.api.AladhanApi
import com.example.islam.domain.model.PrayerTime
import com.example.islam.domain.repository.PrayerTimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class PrayerTimeRepositoryImpl @Inject constructor(
    private val dao: PrayerTimeDao,
    private val api: AladhanApi
) : PrayerTimeRepository {

    /**
     * Offline-First Mantığı:
     *
     * 1. Güncel ay için DB'de ≥28 kayıt varsa → ağa çıkma, sadece DB'den bugünü döndür
     * 2. Kayıt yetersizse → /calendarByCity ile tüm ayı tek seferde çek
     *    a. Başarılıysa → insertAll() ile hepsini DB'ye yaz, geçmiş ayları temizle, bugünü döndür
     *    b. Hata varsa → DB'de yine de bugün var mı? Varsa stale fallback döndür
     *    c. DB'de de bugün yoksa → Resource.Error
     *
     * NOT: method/school değiştiğinde cache'i otomatik geçersiz kılmıyoruz.
     * Refresh butonu veya ayın değişmesi cache'i temizler.
     */
    override suspend fun getPrayerTimes(
        city: String,
        country: String,
        method: Int,
        school: Int,
        date: String?
    ): Resource<PrayerTime> {
        val queryDate = date ?: DateUtil.todayFormatted()
        val cal       = Calendar.getInstance()
        val month     = cal.get(Calendar.MONTH) + 1        // Calendar.MONTH 0-indexed
        val year      = cal.get(Calendar.YEAR)

        // ─── 1. Ay önbelleğini kontrol et ────────────────────────────────────
        val cachedCount = dao.countForMonth(month, year, city)
        if (cachedCount >= 28) {
            val todayCached = dao.getPrayerTimeByDate(queryDate, city)
            if (todayCached != null) return Resource.Success(todayCached.toDomain())
            // Ay kayıtları var ama bugünkü tarih eşleşmedi (ay geçiş kenarı) → API'ye düş
        }

        // ─── 2. API'den tüm ayı çek ──────────────────────────────────────────
        val result = runCatching {
            kotlinx.coroutines.withTimeout(10_000L) {
                api.getPrayerCalendarByCity(
                    city    = city,
                    country = country,
                    method  = method,
                    school  = school,
                    month   = month,
                    year    = year
                )
            }
        }

        return result.fold(
            onSuccess = { calendarResponse ->
                // Tüm günleri entity listesine dönüştür ve DB'ye toplu yaz
                val entities = calendarResponse.data.map { it.toEntity(city, country) }
                dao.insertAll(entities)

                // Geçmiş aylara ait eski kayıtları temizle (DB şişmesini önle)
                dao.clearOldMonths(year, month)

                // DB'den bugünü döndür (Single Source of Truth)
                val today = dao.getPrayerTimeByDate(queryDate, city)
                if (today != null) {
                    Resource.Success(today.toDomain())
                } else {
                    Resource.Error("Bugünün vakitleri API yanıtında bulunamadı.")
                }
            },
            onFailure = { e ->
                // ─── 3. Ağ hatası veya Zaman Aşımı → Stale fallback ─────────
                val stale = dao.getPrayerTimeByDate(queryDate, city)
                if (stale != null) {
                    Resource.Success(stale.toDomain())
                } else {
                    Resource.Error(
                        message   = e.localizedMessage ?: "Namaz vakitleri alınamadı.",
                        throwable = e
                    )
                }
            }
        )
    }

    override fun getPrayerTimesFlow(city: String): Flow<List<PrayerTime>> =
        dao.getPrayerTimesFlow(city).map { list -> list.map { it.toDomain() } }

    override suspend fun deleteOldPrayerTimes(beforeDate: String) {
        dao.deleteOldPrayerTimes(beforeDate)
    }
}
