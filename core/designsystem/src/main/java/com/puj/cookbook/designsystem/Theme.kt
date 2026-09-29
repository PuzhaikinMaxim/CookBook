package com.puj.cookbook.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Цветовая схема для светлой темы. */
private val LightColors = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = Warm40,
)

/** Цветовая схема для тёмной темы. */
private val DarkColors = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = Warm80,
)

/**
 * Тема приложения CookBook.
 *
 * @param darkTheme использовать ли тёмную тему; по умолчанию следует системной настройке.
 * @param content содержимое, к которому применяется тема.
 */
@Composable
fun CookBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CookBookTypography,
        content = content,
    )
}
