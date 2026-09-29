package com.puj.cookbook.core

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * Базовый текст приложения поверх [BasicText] с типографикой и цветом из темы.
 *
 * @param text отображаемый текст.
 * @param modifier модификатор.
 * @param style стиль текста; по умолчанию основной текст темы.
 * @param color цвет текста; по умолчанию основной цвет темы.
 * @param textAlign выравнивание текста.
 * @param maxLines максимальное число строк.
 * @param overflow поведение при переполнении.
 */
@Composable
fun CookBookText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = CookBookTheme.typography.body,
    color: Color = CookBookTheme.colors.textPrimary,
    textAlign: TextAlign = TextAlign.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = color, textAlign = textAlign),
        maxLines = maxLines,
        overflow = overflow,
    )
}
