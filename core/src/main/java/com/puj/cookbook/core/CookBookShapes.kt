package com.puj.cookbook.core

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Набор форм (скруглений) приложения. */
@Immutable
data class CookBookShapes(
    /** Малое скругление: поля ввода, чекбоксы. */
    val small: Shape,
    /** Среднее скругление: кнопки. */
    val medium: Shape,
    /** Большое скругление: карточки, изображения. */
    val large: Shape,
    /** Форма-таблетка: чипы. */
    val pill: Shape,
)

/** Формы по умолчанию. */
val DefaultCookBookShapes = CookBookShapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    pill = RoundedCornerShape(percent = 50),
)

/** Локальный провайдер форм. */
val LocalCookBookShapes = staticCompositionLocalOf { DefaultCookBookShapes }
