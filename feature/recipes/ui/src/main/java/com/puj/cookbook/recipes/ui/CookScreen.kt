package com.puj.cookbook.recipes.ui

import androidx.hilt.navigation.compose.hiltViewModel

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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puj.cookbook.designsystem.RecipePicture
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onStartCooking: (Long) -> Unit,
) {
    val vm: CookViewModel = hiltViewModel()
    val recipe by vm.recipe.collectAsStateWithLifecycle()
    val timerStates by vm.timerStates.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title ?: "") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
                },
                actions = {
                    recipe?.let { r ->
                        TextButton(onClick = { onEdit(r.id) }) { Text(stringResource(R.string.action_edit)) }
                    }
                },
            )
        },
    ) { innerPadding ->
        val current = recipe
        if (current == null) {
            Column(Modifier.fillMaxSize().padding(innerPadding), verticalArrangement = Arrangement.Center) {
                Text(stringResource(R.string.recipe_not_found), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        } else {
            RecipeBody(
                recipe = current,
                timerStates = timerStates,
                onToggleItem = { itemId, checked -> vm.setCheckItemChecked(itemId, checked) },
                onToggleTimer = vm::toggleTimer,
                onResetTimer = vm::resetTimer,
                onStartCooking = { onStartCooking(current.id) },
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

/** Тело экрана рецепта: описание, кнопка запуска готовки и последовательность блоков. */
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
        if (recipe.description.isNotBlank()) {
            Text(recipe.description, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
        }
        if (recipe.blocks.isNotEmpty()) {
            Button(onClick = onStartCooking, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cook_start))
            }
            Spacer(Modifier.height(8.dp))
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
                HorizontalDivider(Modifier.padding(vertical = 12.dp))
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
        is TextBlock -> Text(block.text, style = MaterialTheme.typography.bodyLarge)
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
            Text(block.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        block.items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { checked -> onToggleItem(item.id, checked) },
                )
                Text(
                    item.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
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
