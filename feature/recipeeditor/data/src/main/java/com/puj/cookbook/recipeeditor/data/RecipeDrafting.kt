package com.puj.cookbook.recipeeditor.data

import com.puj.cookbook.recipes.domain.CheckItem
import com.puj.cookbook.recipes.domain.ChecklistBlock
import com.puj.cookbook.recipes.domain.PictureBlock
import com.puj.cookbook.recipes.domain.Recipe
import com.puj.cookbook.recipes.domain.RecipeBlock
import com.puj.cookbook.recipes.domain.TextBlock
import com.puj.cookbook.recipes.domain.TimerBlock

/**
 * Чистая (без зависимости от Android) логика редактора, которая превращает действия
 * пользователя в новый экземпляр [Recipe]. Находится в data-модуле редактора, чтобы её
 * можно было переиспользовать в других экранах.
 */
object RecipeDrafting {

    /** Создаёт новый пустой рецепт с заданным [title]. */
    fun newRecipe(title: String = ""): Recipe {
        return Recipe(title = title)
    }

    /** Возвращает копию рецепта с новым названием [title]. */
    fun withTitle(recipe: Recipe, title: String): Recipe {
        return recipe.copy(title = title)
    }

    /** Возвращает копию рецепта с новым описанием [description]. */
    fun withDescription(recipe: Recipe, description: String): Recipe {
        return recipe.copy(description = description)
    }

    /** Добавляет в конец рецепта текстовый блок с текстом [text]. */
    fun addText(recipe: Recipe, text: String = ""): Recipe {
        return recipe.copy(blocks = recipe.blocks + TextBlock(text = text))
    }

    /** Добавляет в конец рецепта блок-изображение по пути [path]. */
    fun addPicture(recipe: Recipe, path: String): Recipe {
        return recipe.copy(blocks = recipe.blocks + PictureBlock(imagePath = path))
    }

    /** Добавляет в конец рецепта список-чеклист с заголовком [title] и одним пустым пунктом. */
    fun addChecklist(recipe: Recipe, title: String): Recipe {
        val items = listOf(CheckItem(text = ""))
        return recipe.copy(blocks = recipe.blocks + ChecklistBlock(title = title, items = items))
    }

    /** Добавляет в конец рецепта таймер с подписью [label] и длительностью [seconds]. */
    fun addTimer(recipe: Recipe, label: String, seconds: Long): Recipe {
        return recipe.copy(blocks = recipe.blocks + TimerBlock(label = label, seconds = seconds))
    }

    /** Заменяет блок на позиции [index] на новый [block]. */
    fun updateBlock(recipe: Recipe, index: Int, block: RecipeBlock): Recipe {
        val blocks = recipe.blocks.toMutableList()
        blocks[index] = block
        return recipe.copy(blocks = blocks)
    }

    /** Удаляет блок на позиции [index]. */
    fun removeBlock(recipe: Recipe, index: Int): Recipe {
        val blocks = recipe.blocks.toMutableList()
        blocks.removeAt(index)
        return recipe.copy(blocks = blocks)
    }

    /** Перемещает блок с позиции [from] на позицию [to]. */
    fun moveBlock(recipe: Recipe, from: Int, to: Int): Recipe {
        if (from !in recipe.blocks.indices || to !in recipe.blocks.indices) {
            return recipe
        }
        val blocks = recipe.blocks.toMutableList()
        val item = blocks.removeAt(from)
        blocks.add(to, item)
        return recipe.copy(blocks = blocks)
    }

    /** Меняет текст пункта [itemIndex] в чеклисте на позиции [blockIndex]. */
    fun updateCheckItem(recipe: Recipe, blockIndex: Int, itemIndex: Int, text: String): Recipe {
        val block = recipe.blocks.getOrNull(blockIndex) as? ChecklistBlock ?: return recipe
        val items = block.items.toMutableList()
        if (itemIndex in items.indices) {
            items[itemIndex] = items[itemIndex].copy(text = text)
        }
        return updateBlock(recipe, blockIndex, block.copy(items = items))
    }

    /** Добавляет новый пустой пункт в чеклист на позиции [blockIndex]. */
    fun addCheckItem(recipe: Recipe, blockIndex: Int): Recipe {
        val block = recipe.blocks.getOrNull(blockIndex) as? ChecklistBlock ?: return recipe
        val items = block.items + CheckItem(text = "")
        return updateBlock(recipe, blockIndex, block.copy(items = items))
    }

    /** Удаляет пункт [itemIndex] из чеклиста на позиции [blockIndex]. */
    fun removeCheckItem(recipe: Recipe, blockIndex: Int, itemIndex: Int): Recipe {
        val block = recipe.blocks.getOrNull(blockIndex) as? ChecklistBlock ?: return recipe
        val items = block.items.toMutableList()
        if (itemIndex in items.indices) {
            items.removeAt(itemIndex)
        }
        return updateBlock(recipe, blockIndex, block.copy(items = items))
    }
}
