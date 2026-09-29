package com.puj.cookbook.recipes.ui

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.puj.cookbook.core.CookBookCard
import com.puj.cookbook.core.CookBookFab
import com.puj.cookbook.core.CookBookIconButton
import com.puj.cookbook.core.CookBookScaffold
import com.puj.cookbook.core.CookBookText
import com.puj.cookbook.core.CookBookTextField
import com.puj.cookbook.core.CookBookTheme
import com.puj.cookbook.core.CookBookTopBar
import com.puj.cookbook.core.R as CoreR
import com.puj.cookbook.core.RecipeThumbnail
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

    CookBookScaffold(
        topBar = { CookBookTopBar(title = stringResource(R.string.recipes_title)) },
        floatingActionButton = {
            CookBookFab(
                painter = painterResource(CoreR.drawable.ic_add),
                contentDescription = stringResource(R.string.recipes_add),
                onClick = onAddRecipe,
            )
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (recipes.isNotEmpty()) {
                CookBookTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = stringResource(R.string.recipes_search_hint),
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
                    CookBookText(
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
        Image(
            painter = painterResource(CoreR.drawable.ic_empty_recipes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(140.dp),
        )
        CookBookText(
            text = stringResource(R.string.recipes_empty_title),
            style = CookBookTheme.typography.title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            textAlign = TextAlign.Center,
        )
        CookBookText(
            text = stringResource(R.string.recipes_empty_body),
            style = CookBookTheme.typography.bodySmall,
            color = CookBookTheme.colors.textSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
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
            bottom = 96.dp,
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
    CookBookCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
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
                CookBookText(
                    text = recipe.title,
                    style = CookBookTheme.typography.heading,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (recipe.description.isNotBlank()) {
                    CookBookText(
                        text = recipe.description,
                        style = CookBookTheme.typography.bodySmall,
                        color = CookBookTheme.colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.size(6.dp))
                BlockTypeSummary(recipe)
            }
            CookBookIconButton(
                painter = painterResource(CoreR.drawable.ic_delete),
                contentDescription = stringResource(R.string.action_delete),
                onClick = onDelete,
                tint = CookBookTheme.colors.error,
            )
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
        if (text > 0) SummaryItem(CoreR.drawable.ic_text, text)
        if (pictures > 0) SummaryItem(CoreR.drawable.ic_photo, pictures)
        if (checklists > 0) SummaryItem(CoreR.drawable.ic_checklist, checklists)
        if (timers > 0) SummaryItem(CoreR.drawable.ic_timer, timers)
    }
}

/** Один пункт сводки: иконка типа блока и число, если блоков больше одного. */
@Composable
private fun SummaryItem(iconRes: Int, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            colorFilter = ColorFilter.tint(CookBookTheme.colors.textSecondary),
            modifier = Modifier.size(16.dp),
        )
        if (count > 1) {
            Spacer(Modifier.width(2.dp))
            CookBookText(
                text = count.toString(),
                style = CookBookTheme.typography.caption,
                color = CookBookTheme.colors.textSecondary,
            )
        }
    }
}
