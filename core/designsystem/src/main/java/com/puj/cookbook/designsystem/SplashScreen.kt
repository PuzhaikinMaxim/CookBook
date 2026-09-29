package com.puj.cookbook.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** Тёмный оттенок зелёного в градиенте заставки. */
private val SplashGreenDark = Color(0xFF2E7D32)

/** Светлый оттенок зелёного в градиенте заставки. */
private val SplashGreenLight = Color(0xFF7CB342)

/**
 * Кастомная заставка приложения: кастрюля с анимацией появления, поднимающийся пар
 * и название приложения. По завершении анимации вызывает [onFinished].
 *
 * @param onFinished вызывается, когда анимация заставки закончена.
 * @param modifier модификатор корневого контейнера заставки.
 */
@Composable
fun CookBookSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    /** Прогресс появления кастрюли и текста (от 0 до 1). */
    val entrance = remember { Animatable(0f) }

    /** Бесконечная анимация поднимающегося пара. */
    val steam = rememberInfiniteTransition(label = "splashSteam")
    val steamPhase by steam.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "steamPhase",
    )

    LaunchedEffect(Unit) {
        entrance.animateTo(1f, animationSpec = tween(durationMillis = 800))
        delay(900)
        onFinished()
    }

    val background = Brush.verticalGradient(
        colors = listOf(SplashGreenDark, SplashGreenLight),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center) {
                SteamWisps(
                    phase = steamPhase,
                    modifier = Modifier
                        .size(width = 140.dp, height = 120.dp)
                        .offset(y = (-72).dp),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_cooking_pot),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(150.dp)
                        .scale(0.8f + 0.2f * entrance.value)
                        .alpha(entrance.value),
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.splash_title),
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(entrance.value),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(entrance.value),
            )
        }
    }
}

/**
 * Рисует три поднимающиеся струйки пара. [phase] — бесконечный прогресс анимации.
 */
@Composable
private fun SteamWisps(
    phase: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val strokeWidth = width * 0.035f

        repeat(3) { index ->
            val x = width * (0.28f + 0.22f * index)
            val localPhase = (phase + index * 0.33f) % 1f
            val alpha = 0.55f * (1f - localPhase)

            val path = Path().apply {
                moveTo(x, height)
                cubicTo(
                    x - width * 0.07f, height * 0.72f,
                    x + width * 0.07f, height * 0.5f,
                    x, height * 0.18f,
                )
            }

            withTransform({ translate(left = 0f, top = -height * localPhase) }) {
                drawPath(
                    path = path,
                    color = Color.White.copy(alpha = alpha),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }
    }
}
