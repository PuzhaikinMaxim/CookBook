package com.puj.cookbook.core

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Единая шкала отступов приложения. */
@Immutable
data class CookBookSpacing(
    /** Минимальный отступ (4dp). */
    val xs: Dp,
    /** Малый отступ (8dp). */
    val sm: Dp,
    /** Средний отступ (16dp). */
    val md: Dp,
    /** Большой отступ (24dp). */
    val lg: Dp,
    /** Максимальный отступ (32dp). */
    val xl: Dp,
)

/** Отступы по умолчанию. */
val DefaultCookBookSpacing = CookBookSpacing(
    xs = 4.dp,
    sm = 8.dp,
    md = 16.dp,
    lg = 24.dp,
    xl = 32.dp,
)

/** Локальный провайдер отступов. */
val LocalCookBookSpacing = staticCompositionLocalOf { DefaultCookBookSpacing }
