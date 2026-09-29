package com.puj.cookbook.recipes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.puj.cookbook.designsystem.R as DesignSystemR
import com.puj.cookbook.designsystem.formatClock
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
    Card(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.label.isNotBlank()) {
                Text(
                    text = state.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.ic_timer),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    text = if (state.isFinished) stringResource(R.string.timer_done) else formatClock(state.remainingSeconds),
                    style = MaterialTheme.typography.displaySmall,
                    color = if (state.isFinished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onToggle) {
                    Text(
                        when {
                            state.isFinished -> stringResource(R.string.timer_restart)
                            state.isRunning -> stringResource(R.string.timer_pause)
                            else -> stringResource(R.string.timer_start)
                        }
                    )
                }
                TextButton(onClick = onReset) { Text(stringResource(R.string.timer_reset)) }
            }
        }
    }
}
