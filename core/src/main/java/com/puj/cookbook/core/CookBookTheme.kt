package com.puj.cookbook.core

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Тема приложения CookBook. Собственная система темизации: цвета, формы, отступы
 * и типографика передаются через CompositionLocal и не зависят от Material.
 *
 * @param darkTheme использовать ли тёмную тему; по умолчанию следует системной настройке.
 * @param content содержимое, к которому применяется тема.
 */
@Composable
fun CookBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkCookBookColors else LightCookBookColors

    CompositionLocalProvider(
        LocalCookBookColors provides colors,
        LocalCookBookShapes provides DefaultCookBookShapes,
        LocalCookBookSpacing provides DefaultCookBookSpacing,
        LocalCookBookTypography provides DefaultCookBookTypography,
        content = content,
    )
}

/** Точки доступа к текущим токенам темы: `CookBookTheme.colors`, `.shapes`, `.spacing`, `.typography`. */
object CookBookTheme {
    /** Текущая палитра. */
    val colors: CookBookColors
        @Composable get() = LocalCookBookColors.current

    /** Текущие формы. */
    val shapes: CookBookShapes
        @Composable get() = LocalCookBookShapes.current

    /** Текущие отступы. */
    val spacing: CookBookSpacing
        @Composable get() = LocalCookBookSpacing.current

    /** Текущая типографика. */
    val typography: CookBookTypography
        @Composable get() = LocalCookBookTypography.current
}
