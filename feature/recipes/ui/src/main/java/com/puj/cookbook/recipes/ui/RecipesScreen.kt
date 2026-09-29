package com.puj.cookbook.recipes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puj.cookbook.designsystem.R as DesignSystemR
import com.puj.cookbook.designsystem.RecipeThumbnail
import com.puj.cookbook.recipes.domain.ChecklistBlock
import com.puj.cookbook.recipes.domain.PictureBlock
import com.puj.cookbook.recipes.domain.Recipe
import com.puj.cookbook.recipes.domain.TextBlock
import com.puj.cookbook.recipes.domain.TimerBlock
import com.puj.cookbook.recipes.domain.previewImagePath

/**
 * Экран списка рецептов: поиск, список карточек и кнопка добавления нового рецепта.
 *
 * @param onOpenRecipe открывает рецепт по идентификатору.
 * @param onAddRecipe открывает редактор для создания нового рецепта.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    onOpenRecipe: (Long) -> Unit,
    onAddRecipe: () -> Unit,
) {
    val vm: RecipesViewModel = hiltViewModel()
    val recipes by vm.recipes.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }

    val filtered = remember(recipes, query) {
        if (query.isBlank()) {
            recipes
        } else {
            recipes.filter { recipe ->
                recipe.title.contains(query, ignoreCase = true) ||
                    recipe.description.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.recipes_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecipe) {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.ic_add),
                    contentDescription = stringResource(R.string.recipes_add),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (recipes.isNotEmpty()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text(stringResource(R.string.recipes_search_hint)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            when {
                recipes.isEmpty() -> EmptyRecipes(Modifier.fillMaxSize())
                filtered.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.recipes_search_empty, query),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }

                else -> RecipeList(
                    recipes = filtered,
                    onOpen = onOpenRecipe,
                    onDelete = { id -> vm.delete(id) },
                )
            }
        }
    }
}

/** Заглушка для пустого списка рецептов: иллюстрация и подсказка добавить первый рецепт. */
@Composable
private fun EmptyRecipes(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.Center) {
        Icon(
            painter = painterResource(DesignSystemR.drawable.ic_empty_recipes),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(140.dp),
        )
        Text(
            stringResource(R.string.recipes_empty_title),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.recipes_empty_body),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

/** Список карточек рецептов. */
@Composable
private fun RecipeList(
    recipes: List<Recipe>,
    onOpen: (Long) -> Unit,
    onDelete: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = 88.dp,
            start = 16.dp,
            end = 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(recipes, key = { it.id }) { recipe ->
            RecipeCard(recipe, onClick = { onOpen(recipe.id) }, onDelete = { onDelete(recipe.id) })
        }
    }
}

/** Карточка одного рецепта: миниатюра, название, описание, состав блоков и кнопка удаления. */
@Composable
private fun RecipeCard(recipe: Recipe, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RecipeThumbnail(
                path = recipe.previewImagePath,
                contentDescription = null,
                modifier = Modifier.size(84.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (recipe.description.isNotBlank()) {
                    Text(
                        text = recipe.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.size(6.dp))
                BlockTypeSummary(recipe)
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.ic_delete),
                    contentDescription = stringResource(R.string.action_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

/** Компактная сводка по типам блоков: иконка и количество. */
@Composable
private fun BlockTypeSummary(recipe: Recipe) {
    var text = 0
    var pictures = 0
    var checklists = 0
    var timers = 0
    recipe.blocks.forEach { block ->
        when (block) {
            is TextBlock -> text++
            is PictureBlock -> pictures++
            is ChecklistBlock -> checklists++
            is TimerBlock -> timers++
        }
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (text > 0) SummaryItem(DesignSystemR.drawable.ic_text, text)
        if (pictures > 0) SummaryItem(DesignSystemR.drawable.ic_photo, pictures)
        if (checklists > 0) SummaryItem(DesignSystemR.drawable.ic_checklist, checklists)
        if (timers > 0) SummaryItem(DesignSystemR.drawable.ic_timer, timers)
    }
}

/** Один пункт сводки: иконка типа блока и число, если блоков больше одного. */
@Composable
private fun SummaryItem(iconRes: Int, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        if (count > 1) {
            Spacer(Modifier.width(2.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
