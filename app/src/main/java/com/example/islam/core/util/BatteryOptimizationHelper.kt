package com.example.islam.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * Pil optimizasyonu yönetimini kolaylaştıran yardımcı nesne.
 *
 * Xiaomi (MIUI), Oppo (ColorOS), Huawei (EMUI) ve benzeri Çin menşeli
 * cihazlar agresif pil yönetimi uyguladığından, arka plan servisi ve
 * AlarmManager bazlı ezan bildirimleri susabilmektedir.
 *
 * Çözüm:
 * 1. Standart Android yolu → ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
 * 2. İsteğe bağlı: OEM'e özel ayar ekranı intent'leri (MiPush vb.)
 */
object BatteryOptimizationHelper {

    /**
     * Uygulamanın pil optimizasyonu listesinde olup olmadığını döndürür.
     * true  → Uygulama korunuyor (exempted), uyarı göstermeye gerek yok.
     * false → Kısıtlanıyor, kullanıcıyı yönlendirmek gerekiyor.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Standart Android "Pil Optimizasyonunu Yoksay" iletişim kutusunu açar.
     *
     * ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS bir Activity context'i
     * gerektirir; Composable'dan LocalContext.current ile sağlanır.
     */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        try {
            val intent = Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Bazı cihazlar bu intent'i desteklemez — genel pil ayarları ekranına yönlendir
            openBatterySettings(context)
        }
    }

    /**
     * ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS başarısız olduğunda
     * genel pil ayarları ekranını açar.
     */
    fun openBatterySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Son çare: genel ayarlar
            val intent = Intent(Settings.ACTION_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    // ─── OEM markası tespiti ──────────────────────────────────────────────────

    /** Cihazın Xiaomi/MIUI olup olmadığını döndürür. */
    val isXiaomi: Boolean
        get() = Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)

    /** Cihazın Huawei/Honor olup olmadığını döndürür. */
    val isHuawei: Boolean
        get() = Build.MANUFACTURER.equals("Huawei", ignoreCase = true) ||
                Build.MANUFACTURER.equals("Honor", ignoreCase = true)

    /** Cihazın Oppo/Realme/OnePlus olup olmadığını döndürür. */
    val isOppo: Boolean
        get() = Build.MANUFACTURER.equals("Oppo", ignoreCase = true) ||
                Build.MANUFACTURER.equals("Realme", ignoreCase = true) ||
                Build.MANUFACTURER.equals("OnePlus", ignoreCase = true)

    /** Cihazın Vivo olup olmadığını döndürür. */
    val isVivo: Boolean
        get() = Build.MANUFACTURER.equals("Vivo", ignoreCase = true)

    /**
     * Agresif pil yönetimi uygulayan bilinen OEM markaları.
     * Bu cihazlarda kullanıcıyı özellikle uyarmak gerekir.
     */
    val isAggressiveOem: Boolean
        get() = isXiaomi || isHuawei || isOppo || isVivo

    /**
     * Açıklamalar: Kullanıcıya neden bu izni istediğimizi anlatan metin.
     * Cihaz markasına göre özelleştirilmiş.
     */
    fun getExplanationText(): String = when {
        isXiaomi  -> "Xiaomi cihazlar ezan bildirimlerini engelleyebilir. MIUI Pil Tasarruf özelliğinden uygulamayı muaf tutun."
        isHuawei  -> "Huawei/Honor cihazlar arka plan işlemlerini kısıtlar. Pil yönetiminden uygulamayı 'Korunan' olarak işaretleyin."
        isOppo    -> "Oppo/Realme cihazlar arka plan uygulamalarını kapatabilir. Pil optimizasyonundan muaf tutun."
        isVivo    -> "Vivo cihazlar pil ayarlarında arka plan kısıtlaması yapabilir. Uygulamayı izin verilenler listesine ekleyin."
        else      -> "Ezan bildirimlerinin sorunsuz çalması için uygulamayı pil optimizasyonundan muaf tutun."
    }
}
