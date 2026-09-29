package com.puj.cookbook.recipes.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puj.cookbook.core.CookBookButton
import com.puj.cookbook.core.CookBookButtonStyle
import com.puj.cookbook.core.CookBookCheckbox
import com.puj.cookbook.core.CookBookDivider
import com.puj.cookbook.core.CookBookScaffold
import com.puj.cookbook.core.CookBookText
import com.puj.cookbook.core.CookBookTheme
import com.puj.cookbook.core.CookBookTopBar
import com.puj.cookbook.core.RecipePicture
import com.puj.cookbook.recipes.domain.ChecklistBlock
import com.puj.cookbook.recipes.domain.PictureBlock
import com.puj.cookbook.recipes.domain.Recipe
import com.puj.cookbook.recipes.domain.RecipeBlock
import com.puj.cookbook.recipes.domain.TextBlock
import com.puj.cookbook.recipes.domain.TimerBlock
import com.puj.cookbook.recipes.ui.timer.CookingTimer
import com.puj.cookbook.recipes.ui.timer.CookingTimerController

/**
 * Экран просмотра рецепта: описание, список блоков с чеклистами и таймерами,
 * а также кнопка запуска пошаговой готовки.
 *
 * @param onBack возврат назад.
 * @param onEdit открывает редактор рецепта по идентификатору.
 * @param onStartCooking запускает режим пошаговой готовки по идентификатору.
 */
@Composable
fun CookScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onStartCooking: (Long) -> Unit,
) {
    val vm: CookViewModel = hiltViewModel()
    val recipe by vm.recipe.collectAsStateWithLifecycle()
    val timerStates by vm.timerStates.collectAsStateWithLifecycle()

    CookBookScaffold(
        topBar = {
            CookBookTopBar(
                title = recipe?.title.orEmpty(),
                navigation = {
                    CookBookButton(
                        text = stringResource(R.string.action_back),
                        onClick = onBack,
                        buttonStyle = CookBookButtonStyle.Text,
                    )
                },
                actions = {
                    recipe?.let { current ->
                        CookBookButton(
                            text = stringResource(R.string.action_edit),
                            onClick = { onEdit(current.id) },
                            buttonStyle = CookBookButtonStyle.Text,
                        )
                    }
                },
            )
        },
    ) {
        val current = recipe
        if (current == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                CookBookText(
                    text = stringResource(R.string.recipe_not_found),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            RecipeBody(
                recipe = current,
                timerStates = timerStates,
                onToggleItem = { itemId, checked -> vm.setCheckItemChecked(itemId, checked) },
                onToggleTimer = vm::toggleTimer,
                onResetTimer = vm::resetTimer,
                onStartCooking = { onStartCooking(current.id) },
            )
        }
    }
}

/** Тело экрана рецепта: описание, обложка, кнопка запуска готовки и последовательность блоков. */
@Composable
private fun RecipeBody(
    recipe: Recipe,
    timerStates: Map<String, CookingTimer>,
    onToggleItem: (Long, Boolean) -> Unit,
    onToggleTimer: (TimerBlock) -> Unit,
    onResetTimer: (TimerBlock) -> Unit,
    onStartCooking: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        recipe.coverImagePath?.let { cover ->
            Spacer(Modifier.height(12.dp))
            RecipePicture(path = cover, contentDescription = recipe.title)
            Spacer(Modifier.height(12.dp))
        }
        if (recipe.description.isNotBlank()) {
            CookBookText(
                text = recipe.description,
                style = CookBookTheme.typography.body,
                color = CookBookTheme.colors.textSecondary,
            )
            Spacer(Modifier.height(12.dp))
        }
        if (recipe.blocks.isNotEmpty()) {
            CookBookButton(
                text = stringResource(R.string.cook_start),
                onClick = onStartCooking,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
        }
        recipe.blocks.forEachIndexed { index, block ->
            BlockCard(
                recipeId = recipe.id,
                block = block,
                timerStates = timerStates,
                onToggleItem = onToggleItem,
                onToggleTimer = onToggleTimer,
                onResetTimer = onResetTimer,
            )
            if (index != recipe.blocks.lastIndex) {
                CookBookDivider(Modifier.padding(vertical = 14.dp))
            }
        }
        Spacer(Modifier.height(96.dp))
    }
}

/** Отображает один блок рецепта в зависимости от его типа. */
@Composable
internal fun BlockCard(
    recipeId: Long,
    block: RecipeBlock,
    timerStates: Map<String, CookingTimer>,
    onToggleItem: (Long, Boolean) -> Unit,
    onToggleTimer: (TimerBlock) -> Unit,
    onResetTimer: (TimerBlock) -> Unit,
) {
    when (block) {
        is TextBlock -> CookBookText(text = block.text, style = CookBookTheme.typography.body)
        is PictureBlock -> RecipePicture(path = block.imagePath, contentDescription = block.caption)
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

/** Отображает чеклист с возможностью отмечать пункты галочкой. */
@Composable
internal fun ChecklistBlockView(block: ChecklistBlock, onToggleItem: (Long, Boolean) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        if (block.title.isNotBlank()) {
            CookBookText(
                text = block.title,
                style = CookBookTheme.typography.heading,
            )
            Spacer(Modifier.height(6.dp))
        }
        block.items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CookBookCheckbox(
                    checked = item.isChecked,
                    onCheckedChange = { checked -> onToggleItem(item.id, checked) },
                )
                Spacer(Modifier.height(0.dp))
                CookBookText(
                    text = item.text,
                    style = CookBookTheme.typography.body,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                )
            }
        }
    }
}

/** Отображает таймер блока, беря его состояние из контроллера таймеров. */
@Composable
internal fun TimerBlockView(
    recipeId: Long,
    block: TimerBlock,
    state: CookingTimer?,
    onToggle: () -> Unit,
    onReset: () -> Unit,
) {
    val effective = state ?: CookingTimer(
        key = CookingTimerController.timerKey(recipeId, block.id),
        label = block.label,
        totalSeconds = block.seconds,
        remainingSeconds = block.seconds,
        isRunning = false,
        isFinished = false,
    )
    CookingTimerCard(
        state = effective,
        onToggle = onToggle,
        onReset = onReset,
    )
}
