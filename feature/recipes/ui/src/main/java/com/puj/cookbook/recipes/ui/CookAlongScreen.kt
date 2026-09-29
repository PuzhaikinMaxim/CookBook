package com.puj.cookbook.recipes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puj.cookbook.core.CookBookButton
import com.puj.cookbook.core.CookBookButtonStyle
import com.puj.cookbook.core.CookBookProgressBar
import com.puj.cookbook.core.CookBookScaffold
import com.puj.cookbook.core.CookBookText
import com.puj.cookbook.core.CookBookTheme
import com.puj.cookbook.core.CookBookTopBar
import com.puj.cookbook.core.RecipePicture
import com.puj.cookbook.recipes.domain.ChecklistBlock
import com.puj.cookbook.recipes.domain.PictureBlock
import com.puj.cookbook.recipes.domain.RecipeBlock
import com.puj.cookbook.recipes.domain.TextBlock
import com.puj.cookbook.recipes.domain.TimerBlock
import com.puj.cookbook.recipes.ui.timer.CookingTimer
import com.puj.cookbook.recipes.ui.timer.CookingTimerController

/**
 * Экран пошаговой готовки: показывает по одному блоку рецепта за раз с прогрессом
 * и кнопками перехода между шагами.
 *
 * @param onBack выход из режима готовки.
 * @param onFinish завершение готовки на последнем шаге.
 */
@Composable
fun CookAlongScreen(
    onBack: () -> Unit,
    onFinish: () -> Unit,
) {
    val vm: CookAlongViewModel = hiltViewModel()
    val recipe by vm.recipe.collectAsStateWithLifecycle()
    val stepIndex by vm.stepIndex.collectAsStateWithLifecycle()
    val timerStates by vm.timerStates.collectAsStateWithLifecycle()

    val current = recipe
    val steps: List<RecipeBlock> = current?.blocks.orEmpty()
    val safeIndex = stepIndex.coerceIn(0, (steps.size - 1).coerceAtLeast(0))

    CookBookScaffold(
        topBar = {
            CookBookTopBar(
                title = if (steps.isEmpty()) {
                    current?.title.orEmpty()
                } else {
                    stringResource(R.string.cook_step_progress, safeIndex + 1, steps.size)
                },
                navigation = {
                    CookBookButton(
                        text = stringResource(R.string.action_exit),
                        onClick = onBack,
                        buttonStyle = CookBookButtonStyle.Text,
                    )
                },
            )
        },
    ) {
        if (current == null) {
            CenteredMessage(stringResource(R.string.recipe_not_found))
            return@CookBookScaffold
        }
        if (steps.isEmpty()) {
            CenteredMessage(stringResource(R.string.cook_no_steps))
            return@CookBookScaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            CookBookProgressBar(
                progress = (safeIndex + 1f) / steps.size,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                ) {
                    StepContent(
                        recipeId = current.id,
                        block = steps[safeIndex],
                        timerStates = timerStates,
                        onToggleItem = { id, checked -> vm.setCheckItemChecked(id, checked) },
                        onToggleTimer = vm::toggleTimer,
                        onResetTimer = vm::resetTimer,
                    )
                }
            }

            StepNavigation(
                isFirst = safeIndex == 0,
                isLast = safeIndex == steps.lastIndex,
                onPrevious = vm::previous,
                onNext = { vm.next(steps.size) },
                onFinish = onFinish,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** Отображает содержимое текущего шага в зависимости от типа блока. */
@Composable
private fun StepContent(
    recipeId: Long,
    block: RecipeBlock,
    timerStates: Map<String, CookingTimer>,
    onToggleItem: (Long, Boolean) -> Unit,
    onToggleTimer: (TimerBlock) -> Unit,
    onResetTimer: (TimerBlock) -> Unit,
) {
    when (block) {
        is TextBlock -> CookBookText(
            text = block.text,
            style = CookBookTheme.typography.title,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        is PictureBlock -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RecipePicture(path = block.imagePath, contentDescription = block.caption)
            if (block.caption.isNotBlank()) {
                CookBookText(
                    text = block.caption,
                    style = CookBookTheme.typography.body,
                    color = CookBookTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        is ChecklistBlock -> ChecklistBlockView(block, onToggleItem)

        is TimerBlock -> TimerBlockView(
            recipeId = recipeId,
            block = block,
            state = timerStates[CookingTimerController.timerKey(recipeId, block.id)],
            onToggle = { onToggleTimer(block) },
            onReset = { onResetTimer(block) },
        )
    }
}

/** Нижняя панель навигации по шагам: «Назад», «Далее» или «Готово». */
@Composable
private fun StepNavigation(
    isFirst: Boolean,
    isLast: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            CookBookButton(
                text = stringResource(R.string.action_previous),
                onClick = onPrevious,
                enabled = !isFirst,
                buttonStyle = CookBookButtonStyle.Outlined,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            CookBookButton(
                text = stringResource(if (isLast) R.string.action_finish else R.string.action_next),
                onClick = if (isLast) onFinish else onNext,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** По центру экрана показывает текстовое сообщение. */
@Composable
private fun CenteredMessage(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CookBookText(text = text, textAlign = TextAlign.Center)
    }
}
