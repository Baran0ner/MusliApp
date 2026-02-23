package com.example.islam.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Google Play "Prominent Disclosure" (Açık Beyan) gereksinimi.
 *
 * Kullanıcı sistem izin popuplarıyla karşılaşmadan ÖNCE gösterilmeli.
 * dismissOnBackPress ve dismissOnClickOutside = false olduğu için
 * "Kabul Ediyorum" butonuna basmadan kapatılamaz.
 *
 * Google Play Developer Policy: Personal and Sensitive Information
 * https://support.google.com/googleplay/android-developer/answer/9888076
 */
@Composable
fun ProminentDisclosureDialog(
    onAccept: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* dismiss edilemez, kullanıcı kabul etmeli */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Policy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Veri Kullanımı Hakkında",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Uygulamayı kullanmaya başlamadan önce lütfen aşağıdaki bilgileri okuyun.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                HorizontalDivider()

                DisclosureItem(
                    icon = Icons.Outlined.LocationOn,
                    title = "Konum Verisi (Arka Plan)",
                    description = "Bu uygulama, namaz vakitlerini bulunduğunuz konuma göre hesaplamak için uygulama kapalıyken bile Konum verilerinize (Arka Plan) erişebilir. Bu veri yalnızca namaz vakitlerini hesaplamak amacıyla kullanılır ve hiçbir üçüncü tarafla paylaşılmaz."
                )

                DisclosureItem(
                    icon = Icons.Outlined.NotificationsActive,
                    title = "Arka Plan Bildirimleri",
                    description = "Ezan vakti geldiğinde size sesli bildirim sunabilmek için Arka Plan Servisleri ve Tam Zamanlı Alarmlar kullanılır. Bu özellik, uygulamanın kapalı olduğu durumlarda dahi çalışır."
                )

                DisclosureItem(
                    icon = Icons.Outlined.Timer,
                    title = "Tam Zamanlı Alarm İzni",
                    description = "Namaz vakitlerinin tam saatinde (saniye sapması olmadan) bildirilmesi için cihazınızın tam zamanlı alarm özelliği kullanılır."
                )

                HorizontalDivider()

                Text(
                    text = "Bu verileri toplamak için yasal dayanağımız meşru menfaattir. Gizlilik politikamızı Ayarlar > Gizlilik Politikası bölümünden inceleyebilirsiniz.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )

                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Kabul Ediyorum",
                        modifier = Modifier.padding(vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DisclosureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .size(22.dp)
                .padding(top = 2.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }
}
