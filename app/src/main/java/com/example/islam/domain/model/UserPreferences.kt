package com.example.islam.domain.model

data class UserPreferences(
    val city: String = "Istanbul",
    val country: String = "Turkey",
    val latitude: Double = 41.0082,
    val longitude: Double = 28.9784,
    val calculationMethod: Int = 13,    // 13 = Diyanet İşleri Başkanlığı
    val notificationsEnabled: Boolean = true,
    val useGps: Boolean = false,
    // Tema: 0 = Sistem Varsayılanı, 1 = Koyu Tema, 2 = Açık Tema
    val appTheme: Int = 0,
    val school: Int = 0,                // 0 = Şafii, 1 = Hanefi (İkindi/Asr vakti)
    val language: String = "tr",        // "tr" | "en" | "ar"
    // Hicri takvim sapma miktarı (-2..+2 gün)
    val hijriOffset: Int = 0,
    // Günlük namaz hedefi (1..5)
    val dailyPrayerGoal: Int = 5,
    // Format: "fajr:0,dhuhr:0,asr:0,maghrib:0,isha:0"
    // Değerler: 0=Sessiz, 1=Kısa Uyarı Sesi, 2=Tam Ezan
    val prayerNotifTypes: String = "fajr:0,dhuhr:0,asr:0,maghrib:0,isha:0",
    // Kullanıcıya hitap adı (opsiyonel)
    val displayName: String = "",
    // Uygulama içinde kişisel hitap (selamlama/tebrik) açık mı
    val personalizedAddressingEnabled: Boolean = true,
    // Bildirimlerde isim kullanımı açık mı
    val personalizedNotificationsEnabled: Boolean = true,
    // Home'daki tek seferlik isim istem kartı kapatıldı mı
    val namePromptDismissed: Boolean = false
) {
    // Geriye dönük uyumluluk
    val darkTheme: Boolean get() = appTheme == 1
}
