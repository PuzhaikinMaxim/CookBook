package com.puj.cookbook.recipeeditor.ui

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puj.cookbook.common.RecipeNavArgs
import com.puj.cookbook.recipes.domain.ChecklistBlock
import com.puj.cookbook.recipes.domain.PictureBlock
import com.puj.cookbook.recipes.domain.Recipe
import com.puj.cookbook.recipes.domain.RecipeBlock
import com.puj.cookbook.recipes.domain.RecipeDefaults
import com.puj.cookbook.recipes.domain.RecipeRepository
import com.puj.cookbook.recipes.domain.TextBlock
import com.puj.cookbook.recipes.domain.TimerBlock
import com.puj.cookbook.recipeeditor.data.RecipeDrafting
import com.puj.cookbook.recipeeditor.domain.RecipeImageStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** ViewModel экрана создания и редактирования рецепта. */
@HiltViewModel
class RecipeEditorViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val imageStore: RecipeImageStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** Идентификатор редактируемого рецепта; null для нового рецепта. */
    private val existingRecipeId: Long? =
        savedStateHandle.get<Long>(RecipeNavArgs.RECIPE_ID)?.takeIf { it != RecipeDefaults.UNKNOWN_ID }

    /** Признак того, что открыт существующий рецепт (иначе создаётся новый). */
    val isEditing: Boolean = existingRecipeId != null

    /** Текущий черновик рецепта, изменяемый по мере ввода. */
    private val _recipe = MutableStateFlow(Recipe(title = ""))
    val recipe: StateFlow<Recipe> = _recipe.asStateFlow()

    /** Признак выполняющегося сохранения. */
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    /** Идентификатор строкового ресурса с текущей ошибкой; null, если ошибки нет. */
    private val _error = MutableStateFlow<Int?>(null)
    val error: StateFlow<Int?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            val id = existingRecipeId ?: return@launch
            val loaded = repository.observeById(id).first()
            if (loaded != null) {
                _recipe.value = loaded
            }
        }
    }

    /** Применяет преобразование [transform] к текущему черновику рецепта. */
    private fun update(transform: (Recipe) -> Recipe) {
        _recipe.value = transform(_recipe.value)
    }

    /** Изменяет название рецепта. */
    fun setTitle(value: String) {
        update { recipe -> RecipeDrafting.withTitle(recipe, value) }
    }

    /** Изменяет описание рецепта. */
    fun setDescription(value: String) {
        update { recipe -> RecipeDrafting.withDescription(recipe, value) }
    }

    /** Импортирует выбранное изображение и делает его общей картинкой блюда (обложкой). */
    fun importCoverImage(uri: Uri, displayName: String?) {
        viewModelScope.launch {
            runCatching { imageStore.import(uri, displayName) }
                .onSuccess { path ->
                    update { recipe -> RecipeDrafting.withCoverImage(recipe, path) }
                }
                .onFailure { _error.value = R.string.editor_error_image }
        }
    }

    /** Убирает обложку блюда. */
    fun removeCoverImage() {
        update { recipe -> RecipeDrafting.withCoverImage(recipe, null) }
    }

    /** Добавляет текстовый блок. */
    fun addText() {
        update { recipe -> RecipeDrafting.addText(recipe) }
    }

    /** Добавляет чеклист с заголовком [title]. */
    fun addChecklist(title: String) {
        update { recipe -> RecipeDrafting.addChecklist(recipe, title) }
    }

    /** Добавляет таймер с подписью [label] и длительностью [seconds]. */
    fun addTimer(label: String, seconds: Long) {
        update { recipe -> RecipeDrafting.addTimer(recipe, label, seconds) }
    }

    /** Изменяет текст текстового блока на позиции [index]. */
    fun updateTextBlock(index: Int, value: String) {
        updateBlock(index) { block -> (block as? TextBlock)?.copy(text = value) ?: block }
    }

    /** Изменяет подпись таймера на позиции [index]. */
    fun updateTimerLabel(index: Int, value: String) {
        updateBlock(index) { block -> (block as? TimerBlock)?.copy(label = value) ?: block }
    }

    /** Изменяет длительность таймера на позиции [index]. */
    fun updateTimerSeconds(index: Int, seconds: Long) {
        updateBlock(index) { block -> (block as? TimerBlock)?.copy(seconds = seconds) ?: block }
    }

    /** Изменяет заголовок чеклиста на позиции [index]. */
    fun updateChecklistTitle(index: Int, value: String) {
        updateBlock(index) { block -> (block as? ChecklistBlock)?.copy(title = value) ?: block }
    }

    /** Изменяет текст пункта [itemIndex] в чеклисте на позиции [index]. */
    fun updateCheckItem(index: Int, itemIndex: Int, value: String) {
        update { recipe -> RecipeDrafting.updateCheckItem(recipe, index, itemIndex, value) }
    }

    /** Добавляет пустой пункт в чеклист на позиции [index]. */
    fun addCheckItem(index: Int) {
        update { recipe -> RecipeDrafting.addCheckItem(recipe, index) }
    }

    /** Удаляет пункт [itemIndex] из чеклиста на позиции [index]. */
    fun removeCheckItem(index: Int, itemIndex: Int) {
        update { recipe -> RecipeDrafting.removeCheckItem(recipe, index, itemIndex) }
    }

    /** Изменяет подпись изображения на позиции [index]. */
    fun updatePictureCaption(index: Int, value: String) {
        updateBlock(index) { block -> (block as? PictureBlock)?.copy(caption = value) ?: block }
    }

    /** Перемещает блок на позиции [index] на одну позицию вверх. */
    fun moveUp(index: Int) {
        update { recipe -> RecipeDrafting.moveBlock(recipe, index, index - 1) }
    }

    /** Перемещает блок на позиции [index] на одну позицию вниз. */
    fun moveDown(index: Int) {
        update { recipe -> RecipeDrafting.moveBlock(recipe, index, index + 1) }
    }

    /** Удаляет блок на позиции [index]. */
    fun removeBlock(index: Int) {
        update { recipe -> RecipeDrafting.removeBlock(recipe, index) }
    }

    /**
     * Импортирует выбранное изображение и либо добавляет новый блок-изображение
     * (когда [targetIndex] равен null), либо заменяет изображение существующего блока.
     */
    fun importPicture(targetIndex: Int?, uri: Uri, displayName: String?) {
        viewModelScope.launch {
            runCatching { imageStore.import(uri, displayName) }
                .onSuccess { path ->
                    if (targetIndex == null) {
                        update { recipe -> RecipeDrafting.addPicture(recipe, path) }
                    } else {
                        updateBlock(targetIndex) { block ->
                            (block as? PictureBlock)?.copy(imagePath = path) ?: block
                        }
                    }
                }
                .onFailure { _error.value = R.string.editor_error_image }
        }
    }

    /** Сбрасывает текущую ошибку. */
    fun clearError() {
        _error.value = null
    }

    /** Сохраняет рецепт и вызывает [onSaved] с идентификатором сохранённого рецепта. */
    fun save(onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val title = _recipe.value.title.trim()
            if (title.isEmpty()) {
                _error.value = R.string.editor_error_title_required
                return@launch
            }
            _saving.value = true
            runCatching { repository.save(_recipe.value.copy(title = title)) }
                .onSuccess { id -> onSaved(id) }
                .onFailure { _error.value = R.string.editor_error_save }
            _saving.value = false
        }
    }

    /** Применяет [transform] к блоку на позиции [index], если он существует. */
    private fun updateBlock(index: Int, transform: (RecipeBlock) -> RecipeBlock) {
        val current = _recipe.value.blocks.getOrNull(index) ?: return
        val updated = transform(current)
        if (updated != current) {
            update { recipe -> RecipeDrafting.updateBlock(recipe, index, updated) }
        }
    }
}
