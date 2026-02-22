package com.example.islam.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ──────────────────────────────────────────
// String Extensions
// ──────────────────────────────────────────

fun String.capitalizeFirst(): String =
    if (isEmpty()) this else this[0].uppercaseChar() + substring(1)

fun String?.orDash(): String = if (isNullOrBlank()) "–" else this

// ──────────────────────────────────────────
// Long / Time Extensions
// ──────────────────────────────────────────

fun Long.toFormattedTime(pattern: String = "HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toFormattedDate(pattern: String = "dd MMMM yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale("tr"))
    return sdf.format(Date(this))
}

// ──────────────────────────────────────────
// Context Extensions
// ──────────────────────────────────────────

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// ──────────────────────────────────────────
// Compose Extensions
// ──────────────────────────────────────────

@Composable
fun Dp.toPx(): Float {
    val density = LocalDensity.current
    return with(density) { this@toPx.toPx() }
}
