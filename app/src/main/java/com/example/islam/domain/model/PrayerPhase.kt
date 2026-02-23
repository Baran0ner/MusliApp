package com.example.islam.domain.model

/**
 * Günün namaz vakitlerine göre fazlar — Gökyüzü Yansımaları arka plan gradyanı için.
 * Her faz, vakitler arasındaki zaman dilimine karşılık gelir.
 */
enum class PrayerPhase {
    /** İmsak (fajr) – Güneş (sunrise) arası; sabah alacakaranlığı */
    DAWN,

    /** Öğle – İkindi arası */
    NOON,

    /** İkindi – Akşam arası */
    AFTERNOON,

    /** Akşam – Yatsı arası; gün batımı */
    SUNSET,

    /** Yatsı – İmsak arası; gece */
    NIGHT
}
