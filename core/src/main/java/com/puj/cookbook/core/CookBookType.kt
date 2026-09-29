package com.puj.cookbook.core

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Типографика приложения. Для заголовков используется Serif — это придаёт «книжный»,
 * кулинарный характер, для основного текста — SansSerif.
 */
@Immutable
data class CookBookTypography(
    /** Крупный экранный заголовок (название блюда). */
    val display: TextStyle,
    /** Заголовок экрана. */
    val title: TextStyle,
    /** Подзаголовок блока. */
    val heading: TextStyle,
    /** Основной текст. */
    val body: TextStyle,
    /** Мелкий основной текст. */
    val bodySmall: TextStyle,
    /** Подписи кнопок и полей. */
    val label: TextStyle,
    /** Совсем мелкие подписи. */
    val caption: TextStyle,
)

/** Типографика по умолчанию. */
val DefaultCookBookTypography = CookBookTypography(
    display = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
    ),
    title = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    heading = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 25.sp,
    ),
    body = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    label = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.3.sp,
    ),
    caption = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
)

/** Локальный провайдер типографики. */
val LocalCookBookTypography = staticCompositionLocalOf { DefaultCookBookTypography }
