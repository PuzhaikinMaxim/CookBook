package com.puj.cookbook.core

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Набор цветов приложения. Собственная палитра вместо Material, в «кулинарном» стиле:
 * тёплый бумажный фон, терракотовый акцент, оливковый вторичный цвет.
 */
@Immutable
data class CookBookColors(
    /** Основной фон экранов. */
    val background: Color,
    /** Фон карточек и панелей. */
    val surface: Color,
    /** Приглушённый фон (поля, чипы, дорожки прогресса). */
    val surfaceVariant: Color,
    /** Основной акцентный цвет. */
    val primary: Color,
    /** Цвет текста на основном акценте. */
    val onPrimary: Color,
    /** Вторичный акцент (оливковый). */
    val secondary: Color,
    /** Цвет текста на вторичном акценте. */
    val onSecondary: Color,
    /** Дополнительный тёплый акцент. */
    val accent: Color,
    /** Основной цвет текста. */
    val textPrimary: Color,
    /** Второстепенный цвет текста. */
    val textSecondary: Color,
    /** Цвет границ и разделителей. */
    val outline: Color,
    /** Цвет ошибок. */
    val error: Color,
    /** Цвет текста на фоне ошибки. */
    val onError: Color,
    /** Цвет затемнения поверх контента. */
    val scrim: Color,
    /** Цвет успешного состояния. */
    val success: Color,
)

/** Светлая палитра. */
val LightCookBookColors = CookBookColors(
    background = Color(0xFFFFF8E7),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF3E9D2),
    primary = Color(0xFFC1440E),
    onPrimary = Color(0xFFFFF8E7),
    secondary = Color(0xFF6B8E23),
    onSecondary = Color(0xFFFFFFFF),
    accent = Color(0xFFE0A800),
    textPrimary = Color(0xFF2B2621),
    textSecondary = Color(0xFF6F6558),
    outline = Color(0xFFE3D7BC),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    scrim = Color(0x66000000),
    success = Color(0xFF2E7D32),
)

/** Тёмная палитра. */
val DarkCookBookColors = CookBookColors(
    background = Color(0xFF1B1813),
    surface = Color(0xFF252017),
    surfaceVariant = Color(0xFF322B20),
    primary = Color(0xFFE5825A),
    onPrimary = Color(0xFF2B1508),
    secondary = Color(0xFFA9C46C),
    onSecondary = Color(0xFF1B2208),
    accent = Color(0xFFF0C24B),
    textPrimary = Color(0xFFF3EAD8),
    textSecondary = Color(0xFFB7A98F),
    outline = Color(0xFF4A4033),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    scrim = Color(0x99000000),
    success = Color(0xFF8BC48F),
)

/** Локальный провайдер палитры. */
val LocalCookBookColors = staticCompositionLocalOf { LightCookBookColors }
