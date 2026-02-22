package com.example.islam.data.local

import com.example.islam.domain.model.DailyQuote
import com.example.islam.domain.model.QuoteType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Statik Ayet ve Hadis listesi.
 *
 * Listedeki sıraya göre her güne farklı bir öğe atanır;
 * [GetDailyQuoteUseCase] yılın gününü (dayOfYear) liste boyutuna modüler
 * olarak uygulayarak seçimi yapar.
 *
 * Yeni öğe eklemek için [quotes] listesine ekleme yapmak yeterlidir.
 */
@Singleton
class DailyQuoteDataSource @Inject constructor() {

    val quotes: List<DailyQuote> = listOf(

        // ── Ayetler ───────────────────────────────────────────────────────────

        DailyQuote(
            text = "Allah, O'ndan başka ilah olmayandır. Diridir, kayyumdur. " +
                    "O'nu ne uyuklama ne de uyku tutar. Göklerde ve yerde ne varsa " +
                    "hepsi O'nundur.",
            source = "Bakara, 255 (Âyetü'l-Kürsî)",
            type = QuoteType.AYAH
        ),

        DailyQuote(
            text = "Allah hiçbir kimseye gücünün üstünde bir yük yüklemez. " +
                    "Kazandığı iyilik kendi yararına, yaptığı kötülük kendi zararınadır.",
            source = "Bakara, 286",
            type = QuoteType.AYAH
        ),

        DailyQuote(
            text = "Şüphesiz güçlükle birlikte bir kolaylık vardır. " +
                    "Evet, o güçlükle birlikte bir kolaylık daha vardır.",
            source = "İnşirah, 5-6",
            type = QuoteType.AYAH
        ),

        DailyQuote(
            text = "Kim Allah'a tevekkül ederse O, ona yeter. " +
                    "Şüphesiz Allah, emrini yerine getirendir.",
            source = "Talâk, 3",
            type = QuoteType.AYAH
        ),

        DailyQuote(
            text = "İyi bilin ki kalpler ancak Allah'ı anmakla huzur bulur.",
            source = "Ra'd, 28",
            type = QuoteType.AYAH
        ),

        DailyQuote(
            text = "De ki: Ey kendilerine karşı aşırı giden kullarım! " +
                    "Allah'ın rahmetinden umut kesmeyin. Şüphesiz Allah bütün günahları " +
                    "bağışlar. O, çok bağışlayandır, çok merhamet edendir.",
            source = "Zümer, 53",
            type = QuoteType.AYAH
        ),

        DailyQuote(
            text = "Beni anın ki ben de sizi anayım. Bana şükredin, sakın nankörlük etmeyin.",
            source = "Bakara, 152",
            type = QuoteType.AYAH
        ),

        // ── Hadisler ──────────────────────────────────────────────────────────

        DailyQuote(
            text = "Müslüman, dilinden ve elinden diğer Müslümanların güvende olduğu kişidir.",
            source = "Buhârî, Îmân 4",
            type = QuoteType.HADITH
        ),

        DailyQuote(
            text = "Amellerin Allah'a en sevimli olanı, az da olsa devamlı yapılandır.",
            source = "Buhârî, Rikak 18; Müslim, Müsâfirîn 216",
            type = QuoteType.HADITH
        ),

        DailyQuote(
            text = "Kolaylaştırınız, zorlaştırmayınız; müjdeleyiniz, nefret ettirmeyiniz.",
            source = "Buhârî, İlim 11",
            type = QuoteType.HADITH
        ),

        DailyQuote(
            text = "Kardeşine gülümsemen sadakadır.",
            source = "Tirmizî, Birr 36",
            type = QuoteType.HADITH
        ),

        DailyQuote(
            text = "Güçlü olan kişi güreşte başkasını yenen değil; " +
                    "öfkelendiğinde kendine hâkim olandır.",
            source = "Buhârî, Edeb 76; Müslim, Birr 107",
            type = QuoteType.HADITH
        ),

        DailyQuote(
            text = "Dünya, ahiretin tarlasıdır.",
            source = "Aclûnî, Keşfu'l-Hafâ, II/412",
            type = QuoteType.HADITH
        )
    )
}
