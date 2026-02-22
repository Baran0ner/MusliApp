package com.example.islam.domain.model

data class Dhikr(
    val id: Long = 0,
    val name: String,
    val arabicText: String,
    val meaning: String,
    val count: Int = 0,
    val targetCount: Int = 33
)

/** Pre-defined dhikr list to seed the database */
val defaultDhikrList = listOf(
    Dhikr(name = "Sübhanallah", arabicText = "سُبْحَانَ اللَّهِ", meaning = "Allah'ı tüm eksikliklerden tenzih ederim", targetCount = 33),
    Dhikr(name = "Elhamdülillah", arabicText = "الْحَمْدُ لِلَّهِ", meaning = "Her türlü övgü ve şükür Allah'a aittir", targetCount = 33),
    Dhikr(name = "Allahu Ekber", arabicText = "اللَّهُ أَكْبَرُ", meaning = "Allah her şeyden büyüktür", targetCount = 34),
    Dhikr(name = "La İlahe İllallah", arabicText = "لَا إِلَٰهَ إِلَّا اللَّهُ", meaning = "Allah'tan başka ilah yoktur", targetCount = 100),
    Dhikr(name = "Estağfirullah", arabicText = "أَسْتَغْفِرُ اللَّهَ", meaning = "Allah'tan bağışlanma dilerim", targetCount = 100),
    Dhikr(name = "Salavat", arabicText = "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", meaning = "Allah'ım, Muhammed'e salat eyle", targetCount = 100)
)
