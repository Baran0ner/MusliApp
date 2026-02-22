package com.example.islam.presentation.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.islam.core.i18n.LocalStrings
import com.example.islam.core.util.BatteryOptimizationHelper
import com.example.islam.ui.theme.WarningAmber
import com.example.islam.ui.theme.WarningAmberBg
import com.example.islam.ui.theme.WarningAmberDim
import com.example.islam.ui.theme.WarningAmberFg

// ─── Dil seçenekleri ─────────────────────────────────────────────────────────

private val languageOptions = listOf(
    "tr" to "Türkçe",
    "en" to "English",
    "ar" to "العربية"
)

// ─── Namaz ID ve ekran isimleri ───────────────────────────────────────────────

private val prayerRows = listOf(
    "fajr"    to "İmsak / Sabah",
    "dhuhr"   to "Öğle",
    "asr"     to "İkindi",
    "maghrib" to "Akşam",
    "isha"    to "Yatsı"
)

private val notifTypeOptions = listOf(
    0 to "Sessiz Bildirim",
    1 to "Kısa Uyarı Sesi",
    2 to "Tam Ezan"
)

private val themeOptions = listOf(
    0 to "Sistem Varsayılanı",
    1 to "Koyu Tema",
    2 to "Açık Tema"
)

// ─── Ana ekran ────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state   by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val strings = LocalStrings.current

    val batteryExempted by produceState(initialValue = true, key1 = Unit) {
        value = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
    }

    // ── GPS izin başlatıcı ────────────────────────────────────────────────────
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.fetchGpsLocation()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Başlık
        Text(
            text       = strings.settings,
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )

        // ── Pil Optimizasyonu Uyarı Kartı ─────────────────────────────────────
        if (!batteryExempted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            BatteryWarningBanner(
                titleText   = strings.batteryOptActive,
                buttonText  = strings.batteryOptFix,
                explanation = BatteryOptimizationHelper.getExplanationText(),
                onFixClick  = { BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context) }
            )
        }

        // ── Konum Kartı (GPS) ─────────────────────────────────────────────────
        SettingsCard(title = strings.location) {
            GpsLocationSection(
                locationStatus = state.locationStatus,
                savedText      = state.preferences.let { p ->
                    if (p.city.isNotBlank() && p.city != "Istanbul")
                        "${p.city}  (%.4f, %.4f)".format(p.latitude, p.longitude)
                    else
                        "%.4f, %.4f".format(p.latitude, p.longitude)
                },
                onFetchClick   = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )
        }

        // ── Uygulama Teması ────────────────────────────────────────────────────
        SettingsCard(title = "Uygulama Teması") {
            Text(
                text  = "Uygulamayı istediğiniz tema modunda kullanın.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            IntDropdown(
                label      = "Tema",
                options    = themeOptions,
                selectedId = state.preferences.appTheme,
                onSelected = viewModel::setAppTheme
            )
        }

        // ── Hesaplama Metodu ──────────────────────────────────────────────────
        SettingsCard(title = strings.calculationMethodTitle) {
            Text(
                text  = strings.calculationMethodDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            IntDropdown(
                label      = strings.calculationMethodTitle,
                options    = strings.calcMethods,
                selectedId = state.preferences.calculationMethod,
                onSelected = viewModel::setCalculationMethod
            )
        }

        // ── Mezhep / İkindi Vakti ─────────────────────────────────────────────
        SettingsCard(title = strings.schoolTitle) {
            Text(
                text  = strings.schoolDesc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            IntDropdown(
                label      = strings.schoolTitle,
                options    = strings.schoolOptions,
                selectedId = state.preferences.school,
                onSelected = viewModel::setSchool
            )
        }

        // ── Dil Seçici ────────────────────────────────────────────────────────
        SettingsCard(title = strings.language) {
            LanguageDropdown(
                selectedCode = state.preferences.language,
                onSelected   = viewModel::setLanguage
            )
        }

        // ── Namaz Bildirimleri ────────────────────────────────────────────────
        SettingsCard(title = "Namaz Bildirimleri") {
            // Genel bildirim toggle
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text  = strings.azanNotifications,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked         = state.preferences.notificationsEnabled,
                    onCheckedChange = viewModel::setNotifications,
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor  = MaterialTheme.colorScheme.secondary,
                        checkedTrackColor  = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                    )
                )
            }

            // 5 vakit ayrı bildirim türü
            if (state.preferences.notificationsEnabled) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
                Spacer(Modifier.height(12.dp))

                val notifMap = parsePrayerNotifTypes(state.preferences.prayerNotifTypes)
                prayerRows.forEachIndexed { index, (id, label) ->
                    PrayerNotifRow(
                        prayerLabel    = label,
                        selectedType   = notifMap[id] ?: 0,
                        onTypeSelected = { type -> viewModel.setPrayerNotifType(id, type) }
                    )
                    if (index < prayerRows.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        // ── Hicri Takvim Ayarı ────────────────────────────────────────────────
        SettingsCard(title = "Hicri Takvim Ayarı") {
            Text(
                text  = "Hicri takvimi cihazınıza göre manuel olarak ±2 gün düzenleyin.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(14.dp))
            HijriOffsetStepper(
                offset    = state.preferences.hijriOffset,
                onChanged = viewModel::setHijriOffset
            )
        }

        // ── Hakkında & Yasal ──────────────────────────────────────────────────
        SettingsCard(title = "Hakkında & Yasal") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val uri = android.net.Uri.parse("https://your-privacy-policy-url.com")
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Policy,
                        contentDescription = "Gizlilik Sözleşmesi",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Gizlilik Sözleşmesi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ─── GPS Konum Bölümü ─────────────────────────────────────────────────────────

@Composable
private fun GpsLocationSection(
    locationStatus: LocationStatus,
    savedText: String,
    onFetchClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Mevcut konum gösterimi
        AnimatedVisibility(visible = true) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.secondary,
                    modifier           = Modifier.size(16.dp)
                )
                Text(
                    text  = savedText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
        }

        // Durum mesajı
        AnimatedVisibility(
            visible = locationStatus !is LocationStatus.Idle,
            enter   = fadeIn(tween(300)) + expandVertically(),
            exit    = fadeOut(tween(200)) + shrinkVertically()
        ) {
            val (msg, color) = when (locationStatus) {
                is LocationStatus.Loading -> "Konum alınıyor…" to MaterialTheme.colorScheme.secondary
                is LocationStatus.Success -> "✓ ${locationStatus.displayText}" to Color(0xFF4CAF50)
                is LocationStatus.Error   -> "⚠ ${locationStatus.message}" to WarningAmber
                else                      -> "" to Color.Transparent
            }
            Text(
                text  = msg,
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }

        // GPS Butonu
        Button(
            onClick  = onFetchClick,
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(8.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = MaterialTheme.colorScheme.onPrimary
            ),
            enabled  = locationStatus !is LocationStatus.Loading
        ) {
            if (locationStatus is LocationStatus.Loading) {
                CircularProgressIndicator(
                    modifier  = Modifier.size(18.dp),
                    color     = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector        = Icons.Outlined.GpsFixed,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text("Otomatik Konum Bul (GPS)", fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Namaz Bildirim Satırı ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrayerNotifRow(
    prayerLabel: String,
    selectedType: Int,
    onTypeSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = notifTypeOptions.firstOrNull { it.first == selectedType }?.second ?: ""

    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text     = prayerLabel,
            style    = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        ExposedDropdownMenuBox(
            expanded         = expanded,
            onExpandedChange = { expanded = it },
            modifier         = Modifier.weight(1.4f)
        ) {
            OutlinedTextField(
                value         = selectedLabel,
                onValueChange = {},
                readOnly      = true,
                trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                textStyle = MaterialTheme.typography.bodySmall,
                colors    = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            ExposedDropdownMenu(
                expanded         = expanded,
                onDismissRequest = { expanded = false }
            ) {
                notifTypeOptions.forEach { (type, name) ->
                    DropdownMenuItem(
                        text    = { Text(name, style = MaterialTheme.typography.bodySmall) },
                        onClick = { onTypeSelected(type); expanded = false },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }
    }
}

// ─── Hicri Takvim Stepper ─────────────────────────────────────────────────────

@Composable
private fun HijriOffsetStepper(offset: Int, onChanged: (Int) -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        OutlinedIconButton(
            onClick  = { if (offset > -2) onChanged(offset - 1) },
            enabled  = offset > -2,
            modifier = Modifier.size(42.dp),
            shape    = RoundedCornerShape(8.dp),
            border   = ButtonDefaults.outlinedButtonBorder
        ) {
            Icon(
                imageVector        = Icons.Outlined.Remove,
                contentDescription = "Azalt",
                tint               = if (offset > -2) MaterialTheme.colorScheme.secondary
                                     else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }

        Spacer(Modifier.width(24.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = if (offset > 0) "+$offset" else "$offset",
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.secondary,
                fontSize   = 28.sp
            )
            Text(
                text  = "gün",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        Spacer(Modifier.width(24.dp))

        OutlinedIconButton(
            onClick  = { if (offset < 2) onChanged(offset + 1) },
            enabled  = offset < 2,
            modifier = Modifier.size(42.dp),
            shape    = RoundedCornerShape(8.dp),
            border   = ButtonDefaults.outlinedButtonBorder
        ) {
            Icon(
                imageVector        = Icons.Outlined.Add,
                contentDescription = "Artır",
                tint               = if (offset < 2) MaterialTheme.colorScheme.secondary
                                     else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

// ─── Pil Uyarı Kartı (Amber / Turuncu) ────────────────────────────────────────

@Composable
private fun BatteryWarningBanner(
    titleText: String,
    buttonText: String,
    explanation: String,
    onFixClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(8.dp),
        colors   = CardDefaults.cardColors(containerColor = WarningAmberBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.BatteryAlert,
                    contentDescription = null,
                    tint               = WarningAmberFg,
                    modifier           = Modifier.size(20.dp)
                )
                Text(
                    text       = titleText,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = WarningAmberFg
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text  = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = WarningAmberFg.copy(alpha = 0.85f)
            )

            Spacer(Modifier.height(14.dp))

            Button(
                onClick  = onFixClick,
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = WarningAmberDim,
                    contentColor   = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.BatteryChargingFull,
                    contentDescription = null,
                    modifier           = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(text = buttonText, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─── Genel Ayar Kartı ─────────────────────────────────────────────────────────

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // Başlık satırı
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(
            color     = MaterialTheme.colorScheme.outlineVariant,
            thickness = 0.5.dp
        )
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            content()
        }
    }
}

// ─── Dil Dropdown ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    selectedCode: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = languageOptions.firstOrNull { it.first == selectedCode }?.second ?: ""
    val strings = LocalStrings.current

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = it },
        modifier         = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value         = selectedLabel,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(strings.language) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor    = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languageOptions.forEach { (code, name) ->
                DropdownMenuItem(
                    text    = { Text(name, style = MaterialTheme.typography.bodyMedium) },
                    onClick = { onSelected(code); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

// ─── Genel Int Dropdown ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IntDropdown(
    label: String,
    options: List<Pair<Int, String>>,
    selectedId: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second ?: ""

    ExposedDropdownMenuBox(
        expanded         = expanded,
        onExpandedChange = { expanded = it },
        modifier         = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value         = selectedLabel,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(label) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor    = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text    = { Text(name, style = MaterialTheme.typography.bodyMedium) },
                    onClick = { onSelected(id); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

// ─── Yardımcı: bildirim map'ini parse et ─────────────────────────────────────

private fun parsePrayerNotifTypes(raw: String): Map<String, Int> =
    raw.split(",").associate {
        val parts = it.split(":")
        parts[0] to (parts.getOrElse(1) { "0" }.toIntOrNull() ?: 0)
    }
