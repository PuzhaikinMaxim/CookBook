package com.puj.cookbook.core

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

/**
 * Кастомная карточка: поверхность с контуром и большим скруглением, опционально кликабельная.
 *
 * @param modifier модификатор.
 * @param onClick обработчик нажатия; если null — карточка не кликабельна.
 * @param content содержимое карточки.
 */
@Composable
fun CookBookCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CookBookTheme.colors
    val shape = CookBookTheme.shapes.large
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val overlay by animateColorAsState(
        targetValue = if (pressed) colors.primary.copy(alpha = 0.06f) else Color.Transparent,
        label = "cardOverlay",
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.surface)
            .background(overlay)
            .border(1.dp, colors.outline, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                }
            ),
    ) {
        Column(content = content)
    }
}

/**
 * Кастомный чип-«таблетка» с опциональной иконкой и состоянием выбора.
 *
 * @param label подпись.
 * @param onClick обработчик нажатия.
 * @param modifier модификатор.
 * @param leadingPainter иконка слева.
 * @param selected выбран ли чип.
 */
@Composable
fun CookBookChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingPainter: Painter? = null,
    selected: Boolean = false,
) {
    val colors = CookBookTheme.colors
    val shape = CookBookTheme.shapes.pill
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val container = when {
        selected -> colors.primary
        pressed -> colors.surfaceVariant
        else -> colors.surface
    }
    val contentColor = if (selected) colors.onPrimary else colors.textPrimary

    Row(
        modifier = modifier
            .clip(shape)
            .background(container)
            .border(1.5.dp, if (selected) colors.primary else colors.outline, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (leadingPainter != null) {
            Image(
                painter = leadingPainter,
                contentDescription = null,
                colorFilter = ColorFilter.tint(contentColor),
                modifier = Modifier.size(18.dp),
            )
        }
        CookBookText(
            text = label,
            style = CookBookTheme.typography.label,
            color = contentColor,
        )
    }
}
