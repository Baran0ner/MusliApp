package com.example.islam.presentation.qibla

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ─── Renkler ──────────────────────────────────────────────────────────────────
private val BackgroundDeep  = Color(0xFF091811)   // en derin koyu yeşil
private val BackgroundMid   = Color(0xFF0D2018)   // orta koyu yeşil
private val Gold            = Color(0xFFD4AF37)   // altın
private val GoldFaint       = Color(0x18D4AF37)   // altın %10 (desen)
private val GoldGlow        = Color(0x33D4AF37)   // altın %20 (glow)
private val GoldDim         = Color(0x66D4AF37)   // altın %40
private val TextWhite       = Color.White
private val TextMuted       = Color(0x99FFFFFF)   // %60 beyaz
private val CompassRim      = Color(0xFF1E4A2E)   // koyu yeşil çerçeve
private val CompassInner    = Color(0xFF162B1E)   // pusula içi

// ─── Ana Ekran ────────────────────────────────────────────────────────────────
@Composable
fun QiblaScreen(
    navController: NavController,
    viewModel: QiblaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // bearingToQibla: cihazın şu an hangi açıda döndüğünü hesaba katarak
    // kullanıcının kıbleye ulaşmak için döndürmesi gereken gerçek açıdır.
    // qiblaAngle ise sadece Kabe'nin coğrafi kuzeyden sabit açısıdır — asla değişmez.
    val rawBearing = uiState.compass?.bearingToQibla ?: 0f

    // Yumuşak yay animasyonu — ok titremeden akıcı döner
    val animatedBearing by animateFloatAsState(
        targetValue = rawBearing,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "qiblaArrow"
    )

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDeep),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Gold)
                    Text(
                        text = "Pusula başlatılıyor...",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }
        !uiState.hasSensor -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDeep),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 40.sp
                    )
                    Text(
                        text = "Pusula sensörü bulunamadı",
                        color = Gold,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Bu cihazda manyetometre veya ivmeölçer sensörü mevcut değil.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        else -> {
            QiblaContent(qiblaDegrees = animatedBearing)
        }
    }
}

// ─── İçerik ───────────────────────────────────────────────────────────────────
@Composable
private fun QiblaContent(qiblaDegrees: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BackgroundMid, BackgroundDeep, BackgroundDeep)
                )
            )
    ) {
        // İslami geometrik desen arka plan
        IslamicGeometricBackground()

        // Merkeze yumuşak altın glow
        Box(
            modifier = Modifier
                .size(360.dp)
                .align(Alignment.Center)
                .offset(y = (-20).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(GoldGlow, Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QiblaHeader()
            Spacer(Modifier.weight(1f))
            CompassDial(arrowRotationDeg = qiblaDegrees)
            Spacer(Modifier.height(28.dp))
            QiblaLabel()
            Spacer(Modifier.weight(1f))
        }
    }
}

// ─── Başlık ───────────────────────────────────────────────────────────────────
@Composable
private fun QiblaHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Altın ince çizgi üst
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(1.dp)
                .background(Gold.copy(alpha = 0.5f))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "ٱلْقِبْلَة",
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal,
            color = Gold,
            letterSpacing = 2.sp
        )
        Text(
            text = "KIBLE YÖNÜ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            letterSpacing = 3.sp
        )
        Spacer(Modifier.height(8.dp))
        // Alt ince çizgi
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(1.dp)
                .background(Gold.copy(alpha = 0.5f))
        )
    }
}

// ─── Kıble Etiketi ────────────────────────────────────────────────────────────
@Composable
private fun QiblaLabel() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(1.dp)
                    .background(GoldDim)
            )
            Text(
                text = "Kabe",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Gold,
                letterSpacing = 2.sp
            )
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(1.dp)
                    .background(GoldDim)
            )
        }
        Text(
            text = "İğne Kabe yönünü gösterir",
            fontSize = 10.sp,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
    }
}

// ─── Pusula ───────────────────────────────────────────────────────────────────
@Composable
private fun CompassDial(arrowRotationDeg: Float) {
    val dialSize = 280.dp

    Box(
        modifier = Modifier.size(dialSize),
        contentAlignment = Alignment.Center
    ) {
        // Dış halka — altın gradient
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawGoldRing(size.width / 2, size.height / 2, size.width / 2 - 2.dp.toPx())
        }

        // İç daire — fiziksel derinlik hissi veren radyal gradyan
        Canvas(
            modifier = Modifier
                .size(dialSize - 20.dp)
                .align(Alignment.Center)
        ) {
            // Ana fill: merkez aydınlık → kenar karanlık
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2A5A38),   // aydınlık merkez
                        Color(0xFF1A3A26),   // orta
                        Color(0xFF0D1F14),   // karanlık kenar
                    ),
                    radius = size.width / 2
                )
            )
            // İç gölge vignette — kenarları derinleştiren overlay
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.30f)),
                    radius = size.width / 2,
                )
            )
            // İnce altın iç kenar çizgisi
            drawCircle(
                color = Gold.copy(alpha = 0.35f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Yön harfleri — iç alana
        val innerSize = dialSize - 52.dp
        Box(
            modifier = Modifier.size(innerSize),
            contentAlignment = Alignment.Center
        ) {
            CardinalLabel("K", Alignment.TopCenter, Modifier.padding(top = 6.dp))
            CardinalLabel("D", Alignment.CenterEnd,  Modifier.padding(end = 6.dp))
            CardinalLabel("G", Alignment.BottomCenter, Modifier.padding(bottom = 6.dp))
            CardinalLabel("B", Alignment.CenterStart, Modifier.padding(start = 6.dp))
        }

        // Derece çizgileri
        Canvas(
            modifier = Modifier
                .size(dialSize - 40.dp)
                .align(Alignment.Center)
        ) {
            drawTickMarks()
        }

        // Dönen ok + Kabe ikonu
        Box(
            modifier = Modifier
                .size(dialSize - 20.dp)
                .rotate(arrowRotationDeg),
            contentAlignment = Alignment.Center
        ) {
            // Kabe ikonu — okun ucunda (Canvas ile çizilmiş, şeffaf arka plan)
            KaabaIcon(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 6.dp)
                    .rotate(-arrowRotationDeg)
                    .size(40.dp)
            )

            // Altın ok
            Canvas(
                modifier = Modifier
                    .width(14.dp)
                    .height(100.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = 46.dp)
            ) {
                val path = Path().apply {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width, size.height * 0.85f)
                    lineTo(size.width / 2f, size.height)
                    lineTo(0f, size.height * 0.85f)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(Gold, Color(0xFFA8860E)),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                // Parlama
                val shimmer = Path().apply {
                    moveTo(size.width / 2f, 0f)
                    lineTo(size.width / 2f, size.height * 0.7f)
                    lineTo(0f, size.height * 0.7f)
                    close()
                }
                drawPath(shimmer, Color.White.copy(alpha = 0.20f))
            }
        }

        // Merkez nokta
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Gold, Color(0xFF8B6914))
                    )
                )
                .zIndex(10f)
        )
    }
}

// ─── Canvas ile çizilmiş Kabe ikonu (şeffaf arka plan, beyaz kutu yok) ────────
@Composable
private fun KaabaIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w    = size.width
        val h    = size.height
        val pad  = w * 0.08f

        val bLeft   = pad
        val bRight  = w - pad
        val bTop    = h * 0.10f
        val bBottom = h * 0.92f
        val bWidth  = bRight - bLeft
        val bHeight = bBottom - bTop

        // Bina içi — yarı şeffaf altın dolgu
        drawRoundRect(
            color        = Gold.copy(alpha = 0.18f),
            topLeft      = Offset(bLeft, bTop),
            size         = Size(bWidth, bHeight),
            cornerRadius = CornerRadius(2.dp.toPx())
        )

        // Bina dış çizgisi
        drawRoundRect(
            color        = Gold,
            topLeft      = Offset(bLeft, bTop),
            size         = Size(bWidth, bHeight),
            cornerRadius = CornerRadius(2.dp.toPx()),
            style        = Stroke(width = 1.5.dp.toPx())
        )

        // Kisve altın şeridi (yatay bant)
        val bandTop = bTop + bHeight * 0.30f
        val bandBot = bTop + bHeight * 0.50f
        drawRect(
            color   = Gold.copy(alpha = 0.55f),
            topLeft = Offset(bLeft + 1.5.dp.toPx(), bandTop),
            size    = Size(bWidth - 3.dp.toPx(), bandBot - bandTop)
        )

        // Kapı — kemer şekli
        val doorW = bWidth * 0.28f
        val doorH = bHeight * 0.28f
        val doorL = bLeft + (bWidth - doorW) / 2f
        val doorT = bBottom - doorH - 1.dp.toPx()
        drawRoundRect(
            color        = Gold,
            topLeft      = Offset(doorL, doorT),
            size         = Size(doorW, doorH),
            cornerRadius = CornerRadius(doorW / 2f),
            style        = Stroke(width = 1.dp.toPx())
        )

        // Hafif altın glow
        drawCircle(
            color  = Gold.copy(alpha = 0.10f),
            radius = w * 0.54f,
            center = Offset(w / 2f, h / 2f)
        )
    }
}

@Composable
private fun BoxScope.CardinalLabel(text: String, alignment: Alignment, modifier: Modifier) {
    Text(
        text  = text,
        modifier = Modifier
            .align(alignment)
            .then(modifier),
        style = TextStyle(
            shadow = Shadow(
                color       = Color.Black.copy(alpha = 0.75f),
                offset      = Offset(0f, 1.5f),
                blurRadius  = 5f
            )
        ),
        color      = TextWhite.copy(alpha = 0.95f),
        fontWeight = FontWeight.Bold,
        fontSize   = 15.sp,
        letterSpacing = 0.5.sp
    )
}

// ─── Canvas: Altın halka ───────────────────────────────────────────────────────
private fun DrawScope.drawGoldRing(cx: Float, cy: Float, radius: Float) {
    // Gölge
    drawCircle(
        color = Color.Black.copy(alpha = 0.4f),
        radius = radius + 2.dp.toPx(),
        center = Offset(cx + 4.dp.toPx(), cy + 4.dp.toPx())
    )
    // Dış altın halka
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFE8C96A), Color(0xFFD4AF37), Color(0xFF9A7A10)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        ),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 3.dp.toPx())
    )
}

// ─── Canvas: Derece çizgileri ─────────────────────────────────────────────────
private fun DrawScope.drawTickMarks() {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val outerR = size.width / 2f

    for (i in 0 until 72) {
        val angle = Math.toRadians(i * 5.0)
        val isMajor = i % 9 == 0  // her 45°
        val tickLen = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
        val alpha   = if (isMajor) 0.50f else 0.20f

        val startX = cx + (outerR - tickLen) * cos(angle).toFloat()
        val startY = cy + (outerR - tickLen) * sin(angle).toFloat()
        val endX   = cx + outerR * cos(angle).toFloat()
        val endY   = cy + outerR * sin(angle).toFloat()

        drawLine(
            color = Gold.copy(alpha = alpha),
            start = Offset(startX, startY),
            end   = Offset(endX, endY),
            strokeWidth = if (isMajor) 1.5.dp.toPx() else 0.8.dp.toPx()
        )
    }
}

// ─── İslami Geometrik Arka Plan ───────────────────────────────────────────────
@Composable
private fun IslamicGeometricBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val step = 72.dp.toPx()
        val cols = (size.width / step).toInt() + 2
        val rows = (size.height / step).toInt() + 2

        for (row in -1..rows) {
            for (col in -1..cols) {
                val offsetX = if (row % 2 == 0) 0f else step / 2f
                val cx = col * step + offsetX
                val cy = row * step

                // 8 köşeli yıldız — çok hafif doku (% 5)
                drawStar8(cx, cy, step * 0.28f, Gold.copy(alpha = 0.05f))

                // Çevresinde küçük kareler — neredeyse görünmez (% 3)
                drawSquareDiamond(cx, cy, step * 0.12f, Gold.copy(alpha = 0.03f))
            }
        }
    }
}

private fun DrawScope.drawStar8(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path()
    val innerR = r * 0.45f
    for (i in 0 until 16) {
        val angle = (i * 22.5 * PI / 180.0) - PI / 2
        val radius = if (i % 2 == 0) r else innerR
        val x = cx + (radius * cos(angle)).toFloat()
        val y = cy + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}

private fun DrawScope.drawSquareDiamond(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path().apply {
        moveTo(cx, cy - r)
        lineTo(cx + r, cy)
        lineTo(cx, cy + r)
        lineTo(cx - r, cy)
        close()
    }
    drawPath(path, color)
}

// ─── Preview ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun QiblaPreview() {
    MaterialTheme {
        QiblaContent(qiblaDegrees = 147f)
    }
}
