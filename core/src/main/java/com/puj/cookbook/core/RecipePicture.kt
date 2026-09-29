package com.puj.cookbook.core

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Загружает изображение по пути [path] в память. Возвращает null, если путь пуст
 * или файл не удалось декодировать.
 */
@Composable
private fun rememberRecipeBitmap(path: String?): ImageBitmap? {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, key1 = path) {
        value = if (path == null) {
            null
        } else {
            withContext(Dispatchers.IO) {
                runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull()
            }
        }
    }
    return bitmap
}

/**
 * Загружает и показывает изображение по пути [path] (это файл, сохранённый импортёром фото).
 * Пока идёт декодирование или если файл отсутствует, показывается нейтральная заглушка.
 *
 * @param path абсолютный путь к изображению в приватном хранилище приложения.
 * @param contentDescription текстовое описание для доступности.
 * @param modifier модификатор, применяемый к контейнеру изображения.
 */
@Composable
fun RecipePicture(
    path: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val current = rememberRecipeBitmap(path)
    if (current != null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(CookBookTheme.shapes.large),
        ) {
            Image(
                bitmap = current,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                contentScale = ContentScale.Crop,
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .clip(CookBookTheme.shapes.large)
                .background(CookBookTheme.colors.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            CookBookText(
                text = stringResource(R.string.picture_placeholder),
                style = CookBookTheme.typography.caption,
                color = CookBookTheme.colors.textSecondary,
            )
        }
    }
}

/**
 * Показывает небольшую квадратную миниатюру изображения [path] со скруглёнными углами.
 * Если изображения нет, рисует нейтральную иконку-заглушку.
 *
 * @param path абсолютный путь к изображению; null, если картинки нет.
 * @param contentDescription текстовое описание для доступности.
 * @param modifier модификатор, задающий размер и форму миниатюры.
 */
@Composable
fun RecipeThumbnail(
    path: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val bitmap = rememberRecipeBitmap(path)
    Box(
        modifier = modifier
            .clip(CookBookTheme.shapes.large)
            .background(CookBookTheme.colors.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(R.drawable.ic_photo),
                contentDescription = null,
                colorFilter = ColorFilter.tint(CookBookTheme.colors.textSecondary),
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

/**
 * Форматирует длительность [totalSeconds] в виде mm:ss (или hh:mm:ss, если время больше часа).
 */
fun formatClock(totalSeconds: Long): String {
    val safe = totalSeconds.coerceAtLeast(0L)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val seconds = safe % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
