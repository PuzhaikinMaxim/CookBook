package com.puj.cookbook.recipes.data

import androidx.room.withTransaction
import com.puj.cookbook.recipes.data.local.BlockEntity
import com.puj.cookbook.recipes.data.local.BlockWithItems
import com.puj.cookbook.recipes.data.local.CheckItemEntity
import com.puj.cookbook.recipes.data.local.CookBookDatabase
import com.puj.cookbook.recipes.data.local.RecipeBlockKind
import com.puj.cookbook.recipes.data.local.RecipeEntity
import com.puj.cookbook.recipes.data.local.RecipeWithBlocks
import com.puj.cookbook.recipes.domain.CheckItem
import com.puj.cookbook.recipes.domain.ChecklistBlock
import com.puj.cookbook.recipes.domain.PictureBlock
import com.puj.cookbook.recipes.domain.Recipe
import com.puj.cookbook.recipes.domain.RecipeBlock
import com.puj.cookbook.recipes.domain.RecipeDefaults
import com.puj.cookbook.recipes.domain.RecipeRepository
import com.puj.cookbook.recipes.domain.TextBlock
import com.puj.cookbook.recipes.domain.TimerBlock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRecipeRepository(private val database: CookBookDatabase) : RecipeRepository {

    private val dao = database.recipeDao()

    override fun observeAll(): Flow<List<Recipe>> {
        return dao.observeAll().map { rows ->
            rows.map { row -> row.toDomain() }
        }
    }

    override fun observeById(id: Long): Flow<Recipe?> {
        return dao.observeById(id).map { row ->
            row?.toDomain()
        }
    }

    override suspend fun save(recipe: Recipe): Long {
        return database.withTransaction {
            val now = System.currentTimeMillis()
            val recipeId = if (recipe.id == RecipeDefaults.UNKNOWN_ID) {
                insertNewRecipe(recipe, now)
            } else {
                updateExistingRecipe(recipe, now)
            }

            recipe.blocks.forEachIndexed { position, block ->
                insertBlock(recipeId, position, block)
            }

            recipeId
        }
    }

    private suspend fun insertNewRecipe(recipe: Recipe, now: Long): Long {
        val entity = RecipeEntity(
            title = recipe.title,
            description = recipe.description,
            coverImagePath = recipe.coverImagePath,
            createdAt = recipe.createdAt,
            updatedAt = now,
        )
        return dao.insertRecipe(entity)
    }

    private suspend fun updateExistingRecipe(recipe: Recipe, now: Long): Long {
        val entity = RecipeEntity(
            id = recipe.id,
            title = recipe.title,
            description = recipe.description,
            coverImagePath = recipe.coverImagePath,
            createdAt = recipe.createdAt,
            updatedAt = now,
        )
        dao.updateRecipe(entity)
        // Blocks are replaced wholesale; the recipe id (and therefore its identity) is preserved.
        dao.deleteBlocksForRecipe(recipe.id)
        return recipe.id
    }

    private suspend fun insertBlock(recipeId: Long, position: Int, block: RecipeBlock) {
        val entity = BlockEntity(
            recipeId = recipeId,
            position = position,
            kind = block.kindName(),
            text = (block as? TextBlock)?.text,
            imagePath = (block as? PictureBlock)?.imagePath,
            caption = (block as? PictureBlock)?.caption,
            label = (block as? ChecklistBlock)?.title ?: (block as? TimerBlock)?.label,
            seconds = (block as? TimerBlock)?.seconds,
        )
        val blockId = dao.insertBlock(entity)

        val items = (block as? ChecklistBlock)?.items.orEmpty()
        items.forEachIndexed { itemPosition, item ->
            val itemEntity = CheckItemEntity(
                blockId = blockId,
                position = itemPosition,
                text = item.text,
                isChecked = item.isChecked,
            )
            dao.insertItem(itemEntity)
        }
    }

    override suspend fun delete(id: Long) {
        dao.deleteRecipe(id)
    }

    override suspend fun setCheckItemChecked(itemId: Long, checked: Boolean) {
        dao.setItemChecked(itemId, checked)
    }
}

private fun RecipeBlock.kindName(): String {
    return when (this) {
        is TextBlock -> RecipeBlockKind.TEXT
        is PictureBlock -> RecipeBlockKind.PICTURE
        is ChecklistBlock -> RecipeBlockKind.CHECKLIST
        is TimerBlock -> RecipeBlockKind.TIMER
    }
}

private fun RecipeWithBlocks.toDomain(): Recipe {
    val sortedBlocks = blocks
        .sortedBy { blockWithItems -> blockWithItems.block.position }
        .map { blockWithItems -> blockWithItems.toDomain() }

    return Recipe(
        id = recipe.id,
        title = recipe.title,
        description = recipe.description,
        coverImagePath = recipe.coverImagePath,
        createdAt = recipe.createdAt,
        updatedAt = recipe.updatedAt,
        blocks = sortedBlocks,
    )
}

private fun BlockWithItems.toDomain(): RecipeBlock {
    val sortedItems = items
        .sortedBy { item -> item.position }
        .map { item -> CheckItem(id = item.id, text = item.text, isChecked = item.isChecked) }

    return when (block.kind) {
        RecipeBlockKind.TEXT -> {
            TextBlock(id = block.id, text = block.text.orEmpty())
        }

        RecipeBlockKind.PICTURE -> {
            PictureBlock(
                id = block.id,
                imagePath = block.imagePath.orEmpty(),
                caption = block.caption.orEmpty(),
            )
        }

        RecipeBlockKind.CHECKLIST -> {
            ChecklistBlock(
                id = block.id,
                title = block.label.orEmpty(),
                items = sortedItems,
            )
        }

        RecipeBlockKind.TIMER -> {
            TimerBlock(
                id = block.id,
                label = block.label.orEmpty(),
                seconds = block.seconds ?: RecipeDefaults.UNKNOWN_ID,
            )
        }

        else -> {
            TextBlock(id = block.id, text = "")
        }
    }
}
