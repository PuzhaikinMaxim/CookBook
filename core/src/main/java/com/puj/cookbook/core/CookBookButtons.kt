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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

/** Варианты оформления кнопки [CookBookButton]. */
enum class CookBookButtonStyle {
    /** Залитая акцентным цветом кнопка. */
    Filled,

    /** Кнопка с контуром и прозрачным фоном. */
    Outlined,

    /** Текстовая кнопка без фона и контура. */
    Text,
}

/**
 * Кастомная кнопка с собственными формами и цветами.
 *
 * @param text подпись кнопки.
 * @param onClick обработчик нажатия.
 * @param modifier модификатор.
 * @param enabled доступна ли кнопка.
 * @param buttonStyle вариант оформления.
 */
@Composable
fun CookBookButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonStyle: CookBookButtonStyle = CookBookButtonStyle.Filled,
) {
    val colors = CookBookTheme.colors
    val shape = CookBookTheme.shapes.medium
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val baseContainer = when (buttonStyle) {
        CookBookButtonStyle.Filled -> colors.primary
        CookBookButtonStyle.Outlined -> Color.Transparent
        CookBookButtonStyle.Text -> Color.Transparent
    }
    val contentColor = when (buttonStyle) {
        CookBookButtonStyle.Filled -> colors.onPrimary
        CookBookButtonStyle.Outlined -> colors.primary
        CookBookButtonStyle.Text -> colors.primary
    }
    val container by animateColorAsState(
        targetValue = if (pressed && enabled) baseContainer.blendWith(colors.textPrimary, 0.08f) else baseContainer,
        label = "buttonContainer",
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (enabled) container else baseContainer.copy(alpha = 0.4f))
            .then(
                if (buttonStyle == CookBookButtonStyle.Outlined) {
                    Modifier.border(1.5.dp, colors.primary, shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        CookBookText(
            text = text,
            style = CookBookTheme.typography.label,
            color = if (enabled) contentColor else contentColor.copy(alpha = 0.5f),
        )
    }
}

/**
 * Кастомная кнопка-иконка с круговой зоной нажатия не меньше 48dp.
 *
 * @param painter иконка.
 * @param contentDescription описание для доступности.
 * @param onClick обработчик нажатия.
 * @param modifier модификатор.
 * @param tint цвет иконки.
 */
@Composable
fun CookBookIconButton(
    painter: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = CookBookTheme.colors.textPrimary,
) {
    val colors = CookBookTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val background by animateColorAsState(
        targetValue = if (pressed) colors.surfaceVariant else Color.Transparent,
        label = "iconButtonBackground",
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(tint),
            modifier = Modifier.size(22.dp),
        )
    }
}

/**
 * Круглая кнопка плавающего действия.
 *
 * @param painter иконка.
 * @param contentDescription описание для доступности.
 * @param onClick обработчик нажатия.
 * @param modifier модификатор.
 */
@Composable
fun CookBookFab(
    painter: Painter,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CookBookTheme.colors
    Box(
        modifier = modifier
            .size(60.dp)
            .shadow(10.dp, CircleShape)
            .clip(CircleShape)
            .background(colors.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(colors.onPrimary),
            modifier = Modifier.size(26.dp),
        )
    }
}

/** Смешивает [this] цвет с [other] на долю [fraction] (0..1). */
internal fun Color.blendWith(other: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = red + (other.red - red) * f,
        green = green + (other.green - green) * f,
        blue = blue + (other.blue - blue) * f,
        alpha = alpha + (other.alpha - alpha) * f,
    )
}
