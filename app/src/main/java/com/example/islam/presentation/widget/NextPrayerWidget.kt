package com.example.islam.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.islam.MainActivity

/**
 * Home Screen Widget — Sıradaki Namaz Vakti
 *
 * Tema:    Koyu Zümrüt Yeşili (#1B4332) arka plan, Altın Sarısı (#D4AF37) vurgular
 * Veri:    PreferencesGlanceStateDefinition ile okunur.
 *          PrayerTimeUpdateWorker her güncelleme sonrası WidgetKeys'e yazar,
 *          Glance otomatik olarak widget'ı yeniler.
 * Tıklama: actionStartActivity<MainActivity>() → uygulamayı açar.
 */
class NextPrayerWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*>
        get() = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent { WidgetContent() }
    }
}

// ─── Renk sabitleri ───────────────────────────────────────────────────────────

private val BgColor    = ColorProvider(android.graphics.Color.parseColor("#1B4332"))
private val CardBg     = ColorProvider(android.graphics.Color.parseColor("#2D6A4F"))
private val GoldColor  = ColorProvider(android.graphics.Color.parseColor("#D4AF37"))
private val Gold50     = ColorProvider(android.graphics.Color.parseColor("#80D4AF37"))
private val WhiteColor = ColorProvider(android.graphics.Color.parseColor("#FFFFFFFF"))
private val White70    = ColorProvider(android.graphics.Color.parseColor("#B3FFFFFF"))
private val White50    = ColorProvider(android.graphics.Color.parseColor("#80FFFFFF"))
private val DivColor   = ColorProvider(android.graphics.Color.parseColor("#4DD4AF37"))

// ─── Widget İçeriği ───────────────────────────────────────────────────────────

@Composable
private fun WidgetContent() {
    val prefs      = currentState<Preferences>()
    val prayerName = prefs[WidgetKeys.NEXT_PRAYER_NAME] ?: "—"
    val remaining  = prefs[WidgetKeys.REMAINING_TIME]   ?: "--:--"
    val gregorian  = prefs[WidgetKeys.GREGORIAN_DATE]   ?: ""
    val hijri      = prefs[WidgetKeys.HIJRI_DATE]        ?: ""

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(BgColor)
            .cornerRadius(16)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier            = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            verticalAlignment   = Alignment.CenterVertically
        ) {

            // ── Üst Satır: Hilal + "Sıradaki Vakit" etiketi ──────────────────
            Row(
                modifier            = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.Start,
                verticalAlignment   = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text  = "☽",
                    style = TextStyle(color = GoldColor, fontSize = 14.sp)
                )
                Spacer(GlanceModifier.width(6.dp))
                Text(
                    text  = "Sıradaki Vakit",
                    style = TextStyle(
                        color      = White70,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(GlanceModifier.height(6.dp))

            // ── Namaz İsmi (büyük, altın) ─────────────────────────────────────
            Text(
                text  = prayerName,
                style = TextStyle(
                    color      = GoldColor,
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(GlanceModifier.height(2.dp))

            // ── Kalan Süre ────────────────────────────────────────────────────
            Text(
                text  = remaining,
                style = TextStyle(
                    color      = WhiteColor,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign  = TextAlign.Center
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )

            Spacer(GlanceModifier.height(8.dp))

            // ── Alt Ayıraç ─────────────────────────────────────────────────────
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DivColor)
            ) {}

            Spacer(GlanceModifier.height(6.dp))

            // ── Tarih Satırı: Miladi • Hicri ─────────────────────────────────
            Row(
                modifier            = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment   = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text  = gregorian,
                    style = TextStyle(color = White50, fontSize = 10.sp)
                )
                if (hijri.isNotBlank()) {
                    Text(
                        text  = "  •  ",
                        style = TextStyle(color = White50, fontSize = 10.sp)
                    )
                    Text(
                        text  = hijri,
                        style = TextStyle(color = Gold50, fontSize = 10.sp)
                    )
                }
            }
        }
    }
}
