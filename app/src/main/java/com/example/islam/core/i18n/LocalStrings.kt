package com.example.islam.core.i18n

import androidx.compose.runtime.compositionLocalOf

/** Mevcut dile ait string kaynağını sağlayan CompositionLocal */
val LocalStrings = compositionLocalOf<AppStrings> { TurkishStrings }
