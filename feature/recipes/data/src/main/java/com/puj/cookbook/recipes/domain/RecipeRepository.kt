package com.puj.cookbook.recipes.domain

import kotlinx.coroutines.flow.Flow

/** Единый источник истины для сохранённых рецептов. */
interface RecipeRepository {

    /** Наблюдает за всеми рецептами, отсортированными по времени последнего изменения. */
    fun observeAll(): Flow<List<Recipe>>

    /** Наблюдает за одним рецептом по [id]; возвращает null, если рецепт не найден. */
    fun observeById(id: Long): Flow<Recipe?>

    /**
     * Сохраняет рецепт (вставка нового или полная замена содержимого существующего).
     * Возвращает идентификатор сохранённого рецепта.
     */
    suspend fun save(recipe: Recipe): Long

    /** Удаляет рецепт по [id] вместе со всеми его блоками и пунктами. */
    suspend fun delete(id: Long)

    /** Меняет отметку одного пункта списка, не заменяя весь рецепт целиком. */
    suspend fun setCheckItemChecked(itemId: Long, checked: Boolean)
}
