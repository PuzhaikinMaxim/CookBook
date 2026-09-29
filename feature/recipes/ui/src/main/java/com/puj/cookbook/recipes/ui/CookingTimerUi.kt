package com.puj.cookbook.recipes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.puj.cookbook.core.CookBookButton
import com.puj.cookbook.core.CookBookButtonStyle
import com.puj.cookbook.core.CookBookCard
import com.puj.cookbook.core.CookBookProgressBar
import com.puj.cookbook.core.CookBookText
import com.puj.cookbook.core.CookBookTheme
import com.puj.cookbook.core.R as CoreR
import com.puj.cookbook.core.formatClock
import com.puj.cookbook.recipes.ui.timer.CookingTimer

/**
 * Показывает один таймер обратного отсчёта с кнопками запуска, паузы и сброса.
 * Состояние берётся из контроллера таймеров.
 *
 * @param state текущее состояние таймера.
 * @param onToggle запускает или ставит таймер на паузу.
 * @param onReset сбрасывает таймер к исходной длительности.
 * @param modifier модификатор карточки таймера.
 */
@Composable
fun CookingTimerCard(
    state: CookingTimer,
    onToggle: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CookBookCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.label.isNotBlank()) {
                CookBookText(
                    text = state.label,
                    style = CookBookTheme.typography.heading,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    painter = painterResource(CoreR.drawable.ic_timer),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(CookBookTheme.colors.primary),
                    modifier = Modifier.size(26.dp),
                )
                CookBookText(
                    text = if (state.isFinished) {
                        stringResource(R.string.timer_done)
                    } else {
                        formatClock(state.remainingSeconds)
                    },
                    style = CookBookTheme.typography.display,
                    color = if (state.isFinished) CookBookTheme.colors.primary else CookBookTheme.colors.textPrimary,
                )
            }
            CookBookProgressBar(
                progress = state.progress,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CookBookButton(
                    text = when {
                        state.isFinished -> stringResource(R.string.timer_restart)
                        state.isRunning -> stringResource(R.string.timer_pause)
                        else -> stringResource(R.string.timer_start)
                    },
                    onClick = onToggle,
                    buttonStyle = CookBookButtonStyle.Outlined,
                )
                Spacer(Modifier.width(4.dp))
                CookBookButton(
                    text = stringResource(R.string.timer_reset),
                    onClick = onReset,
                    buttonStyle = CookBookButtonStyle.Text,
                )
            }
        }
    }
}
