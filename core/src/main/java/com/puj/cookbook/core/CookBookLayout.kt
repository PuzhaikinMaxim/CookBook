package com.puj.cookbook.core

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Кастомный каркас экрана: фон темы, системные отступы, верхняя панель, контент и FAB.
 *
 * @param modifier модификатор.
 * @param topBar верхняя панель.
 * @param floatingActionButton плавающая кнопка.
 * @param content содержимое экрана.
 */
@Composable
fun CookBookScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CookBookTheme.colors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            topBar()
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        ) {
            floatingActionButton()
        }
    }
}

/**
 * Кастомная верхняя панель с заголовком, левым и правыми слотами.
 *
 * @param title заголовок.
 * @param modifier модификатор.
 * @param navigation слот слева (например, кнопка «Назад»).
 * @param actions слот справа.
 */
@Composable
fun CookBookTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigation: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = CookBookTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        navigation?.invoke()
        CookBookText(
            text = title,
            style = CookBookTheme.typography.title,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )
        if (actions != null) {
            Row(verticalAlignment = Alignment.CenterVertically, content = actions)
        }
    }
}

/** Кастомный разделитель высотой 1dp. */
@Composable
fun CookBookDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(CookBookTheme.colors.outline),
    )
}

/**
 * Кастомный линейный индикатор прогресса.
 *
 * @param progress значение от 0 до 1.
 * @param modifier модификатор.
 */
@Composable
fun CookBookProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val colors = CookBookTheme.colors
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "progress",
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp),
    ) {
        val radius = size.height / 2f
        drawRoundRect(
            color = colors.surfaceVariant,
            size = size,
            cornerRadius = CornerRadius(radius, radius),
        )
        drawRoundRect(
            color = colors.primary,
            size = Size(size.width * animated, size.height),
            cornerRadius = CornerRadius(radius, radius),
        )
    }
}
