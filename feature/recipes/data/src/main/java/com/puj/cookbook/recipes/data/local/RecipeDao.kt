package com.puj.cookbook.recipes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** DAO для доступа к рецептам, блокам и пунктам чеклистов. */
@Dao
interface RecipeDao {

    /** Наблюдает за всеми рецептами, отсортированными по времени изменения. */
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<RecipeWithBlocks>>

    /** Наблюдает за одним рецептом по [id]. */
    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeById(id: Long): Flow<RecipeWithBlocks?>

    /** Вставляет новый рецепт и возвращает его идентификатор. */
    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    /** Обновляет существующий рецепт. */
    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    /** Вставляет блок и возвращает его идентификатор. */
    @Insert
    suspend fun insertBlock(block: BlockEntity): Long

    /** Вставляет пункт чеклиста и возвращает его идентификатор. */
    @Insert
    suspend fun insertItem(item: CheckItemEntity): Long

    /** Удаляет все блоки рецепта [recipeId] (пункты удаляются каскадно). */
    @Query("DELETE FROM recipe_blocks WHERE recipeId = :recipeId")
    suspend fun deleteBlocksForRecipe(recipeId: Long)

    /** Удаляет рецепт по [id] (блоки и пункты удаляются каскадно). */
    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: Long)

    /** Меняет отметку пункта [itemId] на значение [checked]. */
    @Query("UPDATE checklist_items SET isChecked = :checked WHERE id = :itemId")
    suspend fun setItemChecked(itemId: Long, checked: Boolean)
}
