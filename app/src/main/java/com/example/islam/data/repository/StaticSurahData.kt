package com.example.islam.data.repository

import com.example.islam.domain.model.SurahListModel

/**
 * 114 surah listesi — internet yokken veya API yanıt vermezken anında gösterilir.
 * Kaynak: Alquran.cloud API ile uyumlu (englishName, verse count, revelationType).
 */
object StaticSurahData {

    /** Türkçe sure anlamları (Diyanet vb. uyumlu) */
    private val turkishTranslations = listOf(
        "Açılış", "İnek", "İmran Ailesi", "Kadınlar", "Sofra", "Davar", "Yüksek Yerler", "Ganimetler", "Tövbe", "Yûnus",
        "Hûd", "Yûsuf", "Gök Gürültüsü", "İbrahim", "Taşlık", "Arı", "Gece Yürüyüşü", "Mağara", "Meryem", "Tâ-Hâ",
        "Peygamberler", "Hac", "Müminler", "Işık", "Ölçü", "Şairler", "Karınca", "Kıssalar", "Örümcek", "Rumlar",
        "Lokman", "Secde", "Gruplar", "Sebe", "Yaratıcı", "Yâ-Sîn", "Saf Tutanlar", "Sâd", "Zümreler", "Bağışlayan",
        "Açıklamalar", "Danışma", "Süsler", "Duman", "Diz Çöken", "Ahkâf", "Muhammed", "Fetih", "Odalar", "Kâf",
        "Savuran Rüzgarlar", "Tur Dağı", "Yıldız", "Ay", "Rahman", "Olay", "Demir", "Mücadele", "Toplanma", "Sorgulanan",
        "Saf", "Cuma", "Münafıklar", "Aldanış", "Boşanma", "Yasaklama", "Mülk", "Kalem", "Gerçek", "Yükseliş Yolları",
        "Nuh", "Cin", "Örtünen", "Bürünen", "Kıyamet", "İnsan", "Gönderilenler", "Haber", "Söküp Çıkaranlar", "Surat Astı",
        "Bürüme", "Yarılma", "Ölçüde Hile Yapanlar", "Yarılma", "Burçlar", "Sabah Yıldızı", "En Yüce", "Bürüyen", "Tan", "Şehir",
        "Güneş", "Gece", "Kuşluk", "Açılış", "İncir", "Pıhtı", "Kadir", "Kanıt", "Deprem", "Koşanlar",
        "Vuran", "Çoğalma", "Asır", "Gammaz", "Fil", "Kureyş", "Yardım", "Kevser", "Kâfirler", "Yardım",
        "İplik", "İhlas", "Tan", "İnsanlar"
    )

    /** Türkçe sure adları (liste ve Son Okunan’da gösterim: "Fatiha Suresi", "Fil Suresi" vb.) */
    private val turkishDisplayNames = listOf(
        "Fatiha Suresi", "Bakara Suresi", "Âl-i İmrân Suresi", "Nisâ Suresi", "Mâide Suresi", "En'âm Suresi", "A'râf Suresi", "Enfâl Suresi", "Tevbe Suresi", "Yûnus Suresi",
        "Hûd Suresi", "Yûsuf Suresi", "Ra'd Suresi", "İbrahim Suresi", "Hicr Suresi", "Nahl Suresi", "İsrâ Suresi", "Kehf Suresi", "Meryem Suresi", "Tâ-Hâ Suresi",
        "Enbiyâ Suresi", "Hac Suresi", "Mü'minûn Suresi", "Nûr Suresi", "Furkan Suresi", "Şuarâ Suresi", "Neml Suresi", "Kasas Suresi", "Ankebût Suresi", "Rûm Suresi",
        "Lokman Suresi", "Secde Suresi", "Ahzâb Suresi", "Sebe' Suresi", "Fâtır Suresi", "Yâsin Suresi", "Sâffât Suresi", "Sâd Suresi", "Zümer Suresi", "Mü'min Suresi",
        "Fussilet Suresi", "Şûrâ Suresi", "Zuhruf Suresi", "Duhân Suresi", "Câsiye Suresi", "Ahkâf Suresi", "Muhammed Suresi", "Fetih Suresi", "Hucurât Suresi", "Kâf Suresi",
        "Zâriyât Suresi", "Tûr Suresi", "Necm Suresi", "Kamer Suresi", "Rahmân Suresi", "Vâkıa Suresi", "Hadid Suresi", "Mücâdele Suresi", "Haşr Suresi", "Mümtehine Suresi",
        "Saff Suresi", "Cumua Suresi", "Münâfikûn Suresi", "Teğâbün Suresi", "Talâk Suresi", "Tahrim Suresi", "Mülk Suresi", "Kalem Suresi", "Hâkka Suresi", "Meâric Suresi",
        "Nûh Suresi", "Cin Suresi", "Müzzemmil Suresi", "Müddessir Suresi", "Kıyâme Suresi", "İnsan Suresi", "Mürselât Suresi", "Nebe' Suresi", "Nâzi'ât Suresi", "Abese Suresi",
        "Tekvîr Suresi", "İnfitâr Suresi", "Mutaffifîn Suresi", "İnşikak Suresi", "Bürûc Suresi", "Târık Suresi", "A'lâ Suresi", "Gâşiye Suresi", "Fecr Suresi", "Beled Suresi",
        "Şems Suresi", "Leyl Suresi", "Duhâ Suresi", "İnşirâh Suresi", "Tîn Suresi", "Alak Suresi", "Kadr Suresi", "Beyyine Suresi", "Zilzâl Suresi", "Âdiyât Suresi",
        "Kâria Suresi", "Tekâsür Suresi", "Asr Suresi", "Hümeze Suresi", "Fil Suresi", "Kureyş Suresi", "Mâûn Suresi", "Kevser Suresi", "Kâfirûn Suresi", "Nasr Suresi",
        "Mesed Suresi", "İhlâs Suresi", "Felak Suresi", "Nâs Suresi"
    )

    private val arabicNames = listOf(
        "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", "يونس",
        "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه",
        "الأنبياء", "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم",
        "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر",
        "فصلت", "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات", "ق",
        "الذاريات", "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة", "الحشر", "الممتحنة",
        "الصف", "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج",
        "نوح", "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس",
        "التكوير", "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد",
        "الشمس", "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات",
        "القارعة", "التكاثر", "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر",
        "المسد", "الإخلاص", "الفلق", "الناس"
    )

    /** Sure numarasına göre Türkçe gösterim adı (API listesinde de kullanılabilir). */
    fun getTurkishDisplayName(surahNumber: Int): String? =
        turkishDisplayNames.getOrNull(surahNumber - 1)

    /** Sure numarasına göre Türkçe anlam (Açılış, İnek, Kadınlar vb. — API listesinde de kullanılabilir). */
    fun getTurkishTranslation(surahNumber: Int): String? =
        turkishTranslations.getOrNull(surahNumber - 1)

    fun getStaticSurahList(): List<SurahListModel> = listOf(
        SurahListModel(1, "Al-Faatiha", "Al-Faatiha", "The Opening", 7, "Meccan", arabicNames[0], false, turkishTranslations[0], turkishDisplayNames[0]),
        SurahListModel(2, "Al-Baqara", "Al-Baqara", "The Cow", 286, "Medinan", arabicNames[1], false, turkishTranslations[1], turkishDisplayNames[1]),
        SurahListModel(3, "Aal-i-Imraan", "Aal-i-Imraan", "The Family of Imraan", 200, "Medinan", arabicNames[2], false, turkishTranslations[2], turkishDisplayNames[2]),
        SurahListModel(4, "An-Nisaa", "An-Nisaa", "The Women", 176, "Medinan", arabicNames[3], false, turkishTranslations[3], turkishDisplayNames[3]),
        SurahListModel(5, "Al-Maaida", "Al-Maaida", "The Table", 120, "Medinan", arabicNames[4], false, turkishTranslations[4], turkishDisplayNames[4]),
        SurahListModel(6, "Al-An'aam", "Al-An'aam", "The Cattle", 165, "Meccan", arabicNames[5], false, turkishTranslations[5], turkishDisplayNames[5]),
        SurahListModel(7, "Al-A'raaf", "Al-A'raaf", "The Heights", 206, "Meccan", arabicNames[6], false, turkishTranslations[6], turkishDisplayNames[6]),
        SurahListModel(8, "Al-Anfaal", "Al-Anfaal", "The Spoils of War", 75, "Medinan", arabicNames[7], false, turkishTranslations[7], turkishDisplayNames[7]),
        SurahListModel(9, "At-Tawba", "At-Tawba", "The Repentance", 129, "Medinan", arabicNames[8], false, turkishTranslations[8], turkishDisplayNames[8]),
        SurahListModel(10, "Yunus", "Yunus", "Jonas", 109, "Meccan", arabicNames[9], false, turkishTranslations[9], turkishDisplayNames[9]),
        SurahListModel(11, "Hud", "Hud", "Hud", 123, "Meccan", arabicNames[10], false, turkishTranslations[10], turkishDisplayNames[10]),
        SurahListModel(12, "Yusuf", "Yusuf", "Joseph", 111, "Meccan", arabicNames[11], false, turkishTranslations[11], turkishDisplayNames[11]),
        SurahListModel(13, "Ar-Ra'd", "Ar-Ra'd", "The Thunder", 43, "Medinan", arabicNames[12], false, turkishTranslations[12], turkishDisplayNames[12]),
        SurahListModel(14, "Ibrahim", "Ibrahim", "Abraham", 52, "Meccan", arabicNames[13], false, turkishTranslations[13], turkishDisplayNames[13]),
        SurahListModel(15, "Al-Hijr", "Al-Hijr", "The Rock", 99, "Meccan", arabicNames[14], false, turkishTranslations[14], turkishDisplayNames[14]),
        SurahListModel(16, "An-Nahl", "An-Nahl", "The Bee", 128, "Meccan", arabicNames[15], false, turkishTranslations[15], turkishDisplayNames[15]),
        SurahListModel(17, "Al-Israa", "Al-Israa", "The Night Journey", 111, "Meccan", arabicNames[16], false, turkishTranslations[16], turkishDisplayNames[16]),
        SurahListModel(18, "Al-Kahf", "Al-Kahf", "The Cave", 110, "Meccan", arabicNames[17], false, turkishTranslations[17], turkishDisplayNames[17]),
        SurahListModel(19, "Maryam", "Maryam", "Mary", 98, "Meccan", arabicNames[18], false, turkishTranslations[18], turkishDisplayNames[18]),
        SurahListModel(20, "Taa-Haa", "Taa-Haa", "Taa-Haa", 135, "Meccan", arabicNames[19], false, turkishTranslations[19], turkishDisplayNames[19]),
        SurahListModel(21, "Al-Anbiyaa", "Al-Anbiyaa", "The Prophets", 112, "Meccan", arabicNames[20], false, turkishTranslations[20], turkishDisplayNames[20]),
        SurahListModel(22, "Al-Hajj", "Al-Hajj", "The Pilgrimage", 78, "Medinan", arabicNames[21], false, turkishTranslations[21], turkishDisplayNames[21]),
        SurahListModel(23, "Al-Muminoon", "Al-Muminoon", "The Believers", 118, "Meccan", arabicNames[22], false, turkishTranslations[22], turkishDisplayNames[22]),
        SurahListModel(24, "An-Noor", "An-Noor", "The Light", 64, "Medinan", arabicNames[23], false, turkishTranslations[23], turkishDisplayNames[23]),
        SurahListModel(25, "Al-Furqaan", "Al-Furqaan", "The Criterion", 77, "Meccan", arabicNames[24], false, turkishTranslations[24], turkishDisplayNames[24]),
        SurahListModel(26, "Ash-Shu'araa", "Ash-Shu'araa", "The Poets", 227, "Meccan", arabicNames[25], false, turkishTranslations[25], turkishDisplayNames[25]),
        SurahListModel(27, "An-Naml", "An-Naml", "The Ant", 93, "Meccan", arabicNames[26], false, turkishTranslations[26], turkishDisplayNames[26]),
        SurahListModel(28, "Al-Qasas", "Al-Qasas", "The Stories", 88, "Meccan", arabicNames[27], false, turkishTranslations[27], turkishDisplayNames[27]),
        SurahListModel(29, "Al-Ankaboot", "Al-Ankaboot", "The Spider", 69, "Meccan", arabicNames[28], false, turkishTranslations[28], turkishDisplayNames[28]),
        SurahListModel(30, "Ar-Room", "Ar-Room", "The Romans", 60, "Meccan", arabicNames[29], false, turkishTranslations[29], turkishDisplayNames[29]),
        SurahListModel(31, "Luqman", "Luqman", "Luqman", 34, "Meccan", arabicNames[30], false, turkishTranslations[30], turkishDisplayNames[30]),
        SurahListModel(32, "As-Sajda", "As-Sajda", "The Prostration", 30, "Meccan", arabicNames[31], false, turkishTranslations[31], turkishDisplayNames[31]),
        SurahListModel(33, "Al-Ahzaab", "Al-Ahzaab", "The Clans", 73, "Medinan", arabicNames[32], false, turkishTranslations[32], turkishDisplayNames[32]),
        SurahListModel(34, "Saba", "Saba", "Sheba", 54, "Meccan", arabicNames[33], false, turkishTranslations[33], turkishDisplayNames[33]),
        SurahListModel(35, "Faatir", "Faatir", "The Originator", 45, "Meccan", arabicNames[34], false, turkishTranslations[34], turkishDisplayNames[34]),
        SurahListModel(36, "Yaseen", "Yaseen", "Yaseen", 83, "Meccan", arabicNames[35], false, turkishTranslations[35], turkishDisplayNames[35]),
        SurahListModel(37, "As-Saaffaat", "As-Saaffaat", "Those drawn up in Ranks", 182, "Meccan", arabicNames[36], false, turkishTranslations[36], turkishDisplayNames[36]),
        SurahListModel(38, "Saad", "Saad", "The letter Saad", 88, "Meccan", arabicNames[37], false, turkishTranslations[37], turkishDisplayNames[37]),
        SurahListModel(39, "Az-Zumar", "Az-Zumar", "The Groups", 75, "Meccan", arabicNames[38], false, turkishTranslations[38], turkishDisplayNames[38]),
        SurahListModel(40, "Ghafir", "Ghafir", "The Forgiver", 85, "Meccan", arabicNames[39], false, turkishTranslations[39], turkishDisplayNames[39]),
        SurahListModel(41, "Fussilat", "Fussilat", "Explained in detail", 54, "Meccan", arabicNames[40], false, turkishTranslations[40], turkishDisplayNames[40]),
        SurahListModel(42, "Ash-Shura", "Ash-Shura", "Consultation", 53, "Meccan", arabicNames[41], false, turkishTranslations[41], turkishDisplayNames[41]),
        SurahListModel(43, "Az-Zukhruf", "Az-Zukhruf", "Ornaments of gold", 89, "Meccan", arabicNames[42], false, turkishTranslations[42], turkishDisplayNames[42]),
        SurahListModel(44, "Ad-Dukhaan", "Ad-Dukhaan", "The Smoke", 59, "Meccan", arabicNames[43], false, turkishTranslations[43], turkishDisplayNames[43]),
        SurahListModel(45, "Al-Jaathiya", "Al-Jaathiya", "Crouching", 37, "Meccan", arabicNames[44], false, turkishTranslations[44], turkishDisplayNames[44]),
        SurahListModel(46, "Al-Ahqaf", "Al-Ahqaf", "The Dunes", 35, "Meccan", arabicNames[45], false, turkishTranslations[45], turkishDisplayNames[45]),
        SurahListModel(47, "Muhammad", "Muhammad", "Muhammad", 38, "Medinan", arabicNames[46], false, turkishTranslations[46], turkishDisplayNames[46]),
        SurahListModel(48, "Al-Fath", "Al-Fath", "The Victory", 29, "Medinan", arabicNames[47], false, turkishTranslations[47], turkishDisplayNames[47]),
        SurahListModel(49, "Al-Hujuraat", "Al-Hujuraat", "The Inner Apartments", 18, "Medinan", arabicNames[48], false, turkishTranslations[48], turkishDisplayNames[48]),
        SurahListModel(50, "Qaaf", "Qaaf", "The letter Qaaf", 45, "Meccan", arabicNames[49], false, turkishTranslations[49], turkishDisplayNames[49]),
        SurahListModel(51, "Adh-Dhaariyat", "Adh-Dhaariyat", "The Winnowing Winds", 60, "Meccan", arabicNames[50], false, turkishTranslations[50], turkishDisplayNames[50]),
        SurahListModel(52, "At-Tur", "At-Tur", "The Mount", 49, "Meccan", arabicNames[51], false, turkishTranslations[51], turkishDisplayNames[51]),
        SurahListModel(53, "An-Najm", "An-Najm", "The Star", 62, "Meccan", arabicNames[52], false, turkishTranslations[52], turkishDisplayNames[52]),
        SurahListModel(54, "Al-Qamar", "Al-Qamar", "The Moon", 55, "Meccan", arabicNames[53], false, turkishTranslations[53], turkishDisplayNames[53]),
        SurahListModel(55, "Ar-Rahmaan", "Ar-Rahmaan", "The Beneficent", 78, "Medinan", arabicNames[54], false, turkishTranslations[54], turkishDisplayNames[54]),
        SurahListModel(56, "Al-Waaqia", "Al-Waaqia", "The Inevitable", 96, "Meccan", arabicNames[55], false, turkishTranslations[55], turkishDisplayNames[55]),
        SurahListModel(57, "Al-Hadid", "Al-Hadid", "The Iron", 29, "Medinan", arabicNames[56], false, turkishTranslations[56], turkishDisplayNames[56]),
        SurahListModel(58, "Al-Mujaadila", "Al-Mujaadila", "The Pleading Woman", 22, "Medinan", arabicNames[57], false, turkishTranslations[57], turkishDisplayNames[57]),
        SurahListModel(59, "Al-Hashr", "Al-Hashr", "The Exile", 24, "Medinan", arabicNames[58], false, turkishTranslations[58], turkishDisplayNames[58]),
        SurahListModel(60, "Al-Mumtahana", "Al-Mumtahana", "She that is to be examined", 13, "Medinan", arabicNames[59], false, turkishTranslations[59], turkishDisplayNames[59]),
        SurahListModel(61, "As-Saff", "As-Saff", "The Ranks", 14, "Medinan", arabicNames[60], false, turkishTranslations[60], turkishDisplayNames[60]),
        SurahListModel(62, "Al-Jumu'a", "Al-Jumu'a", "Friday", 11, "Medinan", arabicNames[61], false, turkishTranslations[61], turkishDisplayNames[61]),
        SurahListModel(63, "Al-Munaafiqoon", "Al-Munaafiqoon", "The Hypocrites", 11, "Medinan", arabicNames[62], false, turkishTranslations[62], turkishDisplayNames[62]),
        SurahListModel(64, "At-Taghaabun", "At-Taghaabun", "Mutual Disillusion", 18, "Medinan", arabicNames[63], false, turkishTranslations[63], turkishDisplayNames[63]),
        SurahListModel(65, "At-Talaaq", "At-Talaaq", "Divorce", 12, "Medinan", arabicNames[64], false, turkishTranslations[64], turkishDisplayNames[64]),
        SurahListModel(66, "At-Tahrim", "At-Tahrim", "The Prohibition", 12, "Medinan", arabicNames[65], false, turkishTranslations[65], turkishDisplayNames[65]),
        SurahListModel(67, "Al-Mulk", "Al-Mulk", "The Sovereignty", 30, "Meccan", arabicNames[66], false, turkishTranslations[66], turkishDisplayNames[66]),
        SurahListModel(68, "Al-Qalam", "Al-Qalam", "The Pen", 52, "Meccan", arabicNames[67], false, turkishTranslations[67], turkishDisplayNames[67]),
        SurahListModel(69, "Al-Haaqqa", "Al-Haaqqa", "The Reality", 52, "Meccan", arabicNames[68], false, turkishTranslations[68], turkishDisplayNames[68]),
        SurahListModel(70, "Al-Ma'aarij", "Al-Ma'aarij", "The Ascending Stairways", 44, "Meccan", arabicNames[69], false, turkishTranslations[69], turkishDisplayNames[69]),
        SurahListModel(71, "Nooh", "Nooh", "Noah", 28, "Meccan", arabicNames[70], false, turkishTranslations[70], turkishDisplayNames[70]),
        SurahListModel(72, "Al-Jinn", "Al-Jinn", "The Jinn", 28, "Meccan", arabicNames[71], false, turkishTranslations[71], turkishDisplayNames[71]),
        SurahListModel(73, "Al-Muzzammil", "Al-Muzzammil", "The Enshrouded One", 20, "Meccan", arabicNames[72], false, turkishTranslations[72], turkishDisplayNames[72]),
        SurahListModel(74, "Al-Muddaththir", "Al-Muddaththir", "The Cloaked One", 56, "Meccan", arabicNames[73], false, turkishTranslations[73], turkishDisplayNames[73]),
        SurahListModel(75, "Al-Qiyaama", "Al-Qiyaama", "The Resurrection", 40, "Meccan", arabicNames[74], false, turkishTranslations[74], turkishDisplayNames[74]),
        SurahListModel(76, "Al-Insaan", "Al-Insaan", "Man", 31, "Medinan", arabicNames[75], false, turkishTranslations[75], turkishDisplayNames[75]),
        SurahListModel(77, "Al-Mursalaat", "Al-Mursalaat", "The Emissaries", 50, "Meccan", arabicNames[76], false, turkishTranslations[76], turkishDisplayNames[76]),
        SurahListModel(78, "An-Naba", "An-Naba", "The Announcement", 40, "Meccan", arabicNames[77], false, turkishTranslations[77], turkishDisplayNames[77]),
        SurahListModel(79, "An-Naazi'aat", "An-Naazi'aat", "Those who drag forth", 46, "Meccan", arabicNames[78], false, turkishTranslations[78], turkishDisplayNames[78]),
        SurahListModel(80, "Abasa", "Abasa", "He frowned", 42, "Meccan", arabicNames[79], false, turkishTranslations[79], turkishDisplayNames[79]),
        SurahListModel(81, "At-Takwir", "At-Takwir", "The Overthrowing", 29, "Meccan", arabicNames[80], false, turkishTranslations[80], turkishDisplayNames[80]),
        SurahListModel(82, "Al-Infitaar", "Al-Infitaar", "The Cleaving", 19, "Meccan", arabicNames[81], false, turkishTranslations[81], turkishDisplayNames[81]),
        SurahListModel(83, "Al-Mutaffifin", "Al-Mutaffifin", "Defrauding", 36, "Meccan", arabicNames[82], false, turkishTranslations[82], turkishDisplayNames[82]),
        SurahListModel(84, "Al-Inshiqaaq", "Al-Inshiqaaq", "The Splitting Open", 25, "Meccan", arabicNames[83], false, turkishTranslations[83], turkishDisplayNames[83]),
        SurahListModel(85, "Al-Burooj", "Al-Burooj", "The Constellations", 22, "Meccan", arabicNames[84], false, turkishTranslations[84], turkishDisplayNames[84]),
        SurahListModel(86, "At-Taariq", "At-Taariq", "The Morning Star", 17, "Meccan", arabicNames[85], false, turkishTranslations[85], turkishDisplayNames[85]),
        SurahListModel(87, "Al-A'laa", "Al-A'laa", "The Most High", 19, "Meccan", arabicNames[86], false, turkishTranslations[86], turkishDisplayNames[86]),
        SurahListModel(88, "Al-Ghaashiya", "Al-Ghaashiya", "The Overwhelming", 26, "Meccan", arabicNames[87], false, turkishTranslations[87], turkishDisplayNames[87]),
        SurahListModel(89, "Al-Fajr", "Al-Fajr", "The Dawn", 30, "Meccan", arabicNames[88], false, turkishTranslations[88], turkishDisplayNames[88]),
        SurahListModel(90, "Al-Balad", "Al-Balad", "The City", 20, "Meccan", arabicNames[89], false, turkishTranslations[89], turkishDisplayNames[89]),
        SurahListModel(91, "Ash-Shams", "Ash-Shams", "The Sun", 15, "Meccan", arabicNames[90], false, turkishTranslations[90], turkishDisplayNames[90]),
        SurahListModel(92, "Al-Lail", "Al-Lail", "The Night", 21, "Meccan", arabicNames[91], false, turkishTranslations[91], turkishDisplayNames[91]),
        SurahListModel(93, "Ad-Dhuhaa", "Ad-Dhuhaa", "The Morning Hours", 11, "Meccan", arabicNames[92], false, turkishTranslations[92], turkishDisplayNames[92]),
        SurahListModel(94, "Ash-Sharh", "Ash-Sharh", "The Consolation", 8, "Meccan", arabicNames[93], false, turkishTranslations[93], turkishDisplayNames[93]),
        SurahListModel(95, "At-Tin", "At-Tin", "The Fig", 8, "Meccan", arabicNames[94], false, turkishTranslations[94], turkishDisplayNames[94]),
        SurahListModel(96, "Al-Alaq", "Al-Alaq", "The Clot", 19, "Meccan", arabicNames[95], false, turkishTranslations[95], turkishDisplayNames[95]),
        SurahListModel(97, "Al-Qadr", "Al-Qadr", "The Power, Fate", 5, "Meccan", arabicNames[96], false, turkishTranslations[96], turkishDisplayNames[96]),
        SurahListModel(98, "Al-Bayyina", "Al-Bayyina", "The Evidence", 8, "Medinan", arabicNames[97], false, turkishTranslations[97], turkishDisplayNames[97]),
        SurahListModel(99, "Az-Zalzala", "Az-Zalzala", "The Earthquake", 8, "Medinan", arabicNames[98], false, turkishTranslations[98], turkishDisplayNames[98]),
        SurahListModel(100, "Al-Aadiyaat", "Al-Aadiyaat", "The Chargers", 11, "Meccan", arabicNames[99], false, turkishTranslations[99], turkishDisplayNames[99]),
        SurahListModel(101, "Al-Qaari'a", "Al-Qaari'a", "The Calamity", 11, "Meccan", arabicNames[100], false, turkishTranslations[100], turkishDisplayNames[100]),
        SurahListModel(102, "At-Takaathur", "At-Takaathur", "Competition", 8, "Meccan", arabicNames[101], false, turkishTranslations[101], turkishDisplayNames[101]),
        SurahListModel(103, "Al-Asr", "Al-Asr", "The Declining Day, Epoch", 3, "Meccan", arabicNames[102], false, turkishTranslations[102], turkishDisplayNames[102]),
        SurahListModel(104, "Al-Humaza", "Al-Humaza", "The Traducer", 9, "Meccan", arabicNames[103], false, turkishTranslations[103], turkishDisplayNames[103]),
        SurahListModel(105, "Al-Fil", "Al-Fil", "The Elephant", 5, "Meccan", arabicNames[104], false, turkishTranslations[104], turkishDisplayNames[104]),
        SurahListModel(106, "Quraish", "Quraish", "Quraysh", 4, "Meccan", arabicNames[105], false, turkishTranslations[105], turkishDisplayNames[105]),
        SurahListModel(107, "Al-Maa'un", "Al-Maa'un", "Almsgiving", 7, "Meccan", arabicNames[106], false, turkishTranslations[106], turkishDisplayNames[106]),
        SurahListModel(108, "Al-Kawthar", "Al-Kawthar", "Abundance", 3, "Meccan", arabicNames[107], false, turkishTranslations[107], turkishDisplayNames[107]),
        SurahListModel(109, "Al-Kaafiroon", "Al-Kaafiroon", "The Disbelievers", 6, "Meccan", arabicNames[108], false, turkishTranslations[108], turkishDisplayNames[108]),
        SurahListModel(110, "An-Nasr", "An-Nasr", "Divine Support", 3, "Medinan", arabicNames[109], false, turkishTranslations[109], turkishDisplayNames[109]),
        SurahListModel(111, "Al-Masad", "Al-Masad", "The Palm Fibre", 5, "Meccan", arabicNames[110], false, turkishTranslations[110], turkishDisplayNames[110]),
        SurahListModel(112, "Al-Ikhlaas", "Al-Ikhlaas", "Sincerity", 4, "Meccan", arabicNames[111], false, turkishTranslations[111], turkishDisplayNames[111]),
        SurahListModel(113, "Al-Falaq", "Al-Falaq", "The Dawn", 5, "Meccan", arabicNames[112], false, turkishTranslations[112], turkishDisplayNames[112]),
        SurahListModel(114, "An-Naas", "An-Naas", "Mankind", 6, "Meccan", arabicNames[113], false, turkishTranslations[113], turkishDisplayNames[113])
    )
}
