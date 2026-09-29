package com.puj.cookbook.core

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Кастомное текстовое поле с подписью, плейсхолдером и подсветкой фокуса/ошибки.
 *
 * @param value текущее значение.
 * @param onValueChange обработчик изменения значения.
 * @param modifier модификатор.
 * @param label подпись над полем.
 * @param placeholder подсказка внутри пустого поля.
 * @param singleLine однострочный ли режим.
 * @param minLines минимальное число строк для многострочного поля.
 * @param isError подсветить поле как ошибочное.
 * @param keyboardOptions настройки клавиатуры.
 */
@Composable
fun CookBookTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = CookBookTheme.colors
    val typography = CookBookTheme.typography
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()

    val borderColor = when {
        isError -> colors.error
        focused -> colors.primary
        else -> colors.outline
    }
    val shape = CookBookTheme.shapes.small

    Column(modifier = modifier, verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
        if (label != null) {
            CookBookText(
                text = label,
                style = typography.caption,
                color = if (isError) colors.error else colors.textSecondary,
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = typography.body.copy(color = colors.textPrimary),
            cursorBrush = SolidColor(colors.primary),
            singleLine = singleLine,
            minLines = if (singleLine) 1 else minLines,
            keyboardOptions = keyboardOptions,
            interactionSource = interaction,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.surface)
                .border(1.5.dp, borderColor, shape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty() && placeholder != null) {
                        CookBookText(
                            text = placeholder,
                            style = typography.body,
                            color = colors.textSecondary,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

/**
 * Кастомный чекбокс с нарисованной галочкой.
 *
 * @param checked текущее состояние.
 * @param onCheckedChange обработчик смены состояния.
 * @param modifier модификатор.
 */
@Composable
fun CookBookCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CookBookTheme.colors
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .size(26.dp)
            .clip(shape)
            .background(if (checked) colors.primary else colors.surface)
            .border(2.dp, if (checked) colors.primary else colors.outline, shape)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Canvas(Modifier.size(16.dp)) {
                val width = size.width
                val height = size.height
                val path = Path().apply {
                    moveTo(width * 0.08f, height * 0.55f)
                    lineTo(width * 0.4f, height * 0.86f)
                    lineTo(width * 0.95f, height * 0.18f)
                }
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = width * 0.16f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }
        }
    }
}

/** Небольшая утилита для отрисовки прямоугольника размером [Size] (используется в прогресс-баре). */
internal fun Size.percentWidth(fraction: Float): Size = Size(width * fraction.coerceIn(0f, 1f), height)
