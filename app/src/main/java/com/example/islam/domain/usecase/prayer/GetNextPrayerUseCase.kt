package com.example.islam.domain.usecase.prayer

import com.example.islam.core.util.DateUtil.cleanTime
import com.example.islam.domain.model.Prayer
import com.example.islam.domain.model.PrayerTime
import java.util.Calendar
import javax.inject.Inject

data class NextPrayer(
    val prayer: Prayer,
    val timeString: String,
    val millisUntil: Long
)

class GetNextPrayerUseCase @Inject constructor() {

    operator fun invoke(prayerTime: PrayerTime): NextPrayer {
        val now = Calendar.getInstance()

        val ordered = listOf(
            Prayer.IMSAK to prayerTime.imsak.cleanTime(),
            Prayer.FAJR to prayerTime.fajr.cleanTime(),
            Prayer.SUNRISE to prayerTime.sunrise.cleanTime(),
            Prayer.DHUHR to prayerTime.dhuhr.cleanTime(),
            Prayer.ASR to prayerTime.asr.cleanTime(),
            Prayer.MAGHRIB to prayerTime.maghrib.cleanTime(),
            Prayer.ISHA to prayerTime.isha.cleanTime()
        )

        for ((prayer, timeStr) in ordered) {
            val cal = buildCalendar(timeStr)
            if (cal.after(now)) {
                return NextPrayer(
                    prayer = prayer,
                    timeString = timeStr,
                    millisUntil = cal.timeInMillis - now.timeInMillis
                )
            }
        }

        // All prayers have passed → next is tomorrow's Fajr (approximate as +1 day)
        val fajrCal = buildCalendar(prayerTime.fajr.cleanTime())
        fajrCal.add(Calendar.DAY_OF_YEAR, 1)
        return NextPrayer(
            prayer = Prayer.FAJR,
            timeString = prayerTime.fajr.cleanTime(),
            millisUntil = fajrCal.timeInMillis - now.timeInMillis
        )
    }

    private fun buildCalendar(timeStr: String): Calendar {
        val parts = timeStr.split(":")
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parts[0].toInt())
            set(Calendar.MINUTE, parts[1].toInt())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
