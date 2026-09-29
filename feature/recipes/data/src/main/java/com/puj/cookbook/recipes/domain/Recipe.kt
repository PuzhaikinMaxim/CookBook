package com.puj.cookbook.recipes.domain

/** Общие значения по умолчанию для рецептов и их блоков. */
object RecipeDefaults {
    /** Идентификатор-заглушка для рецепта, который ещё не сохранён в базе. */
    const val UNKNOWN_ID: Long = 0L

    /** Длительность таймера по умолчанию для нового блока-таймера (10 минут). */
    const val DEFAULT_TIMER_SECONDS: Long = 600L
}

/**
 * Рецепт — это упорядоченный набор блоков [RecipeBlock]. Каждый блок описывает то, что
 * повар может добавить при составлении рецепта: текстовое указание, изображение, список
 * пунктов для отметки во время готовки или таймер обратного отсчёта.
 */
data class Recipe(
    /** Идентификатор рецепта; [RecipeDefaults.UNKNOWN_ID], если рецепт ещё не сохранён. */
    val id: Long = RecipeDefaults.UNKNOWN_ID,
    /** Название рецепта. */
    val title: String,
    /** Необязательное описание рецепта. */
    val description: String = "",
    /** Путь к большой общей картинке блюда (обложке); null, если обложки нет. */
    val coverImagePath: String? = null,
    /** Момент создания рецепта в миллисекундах. */
    val createdAt: Long = System.currentTimeMillis(),
    /** Момент последнего изменения рецепта в миллисекундах. */
    val updatedAt: Long = System.currentTimeMillis(),
    /** Блоки рецепта в порядке отображения. */
    val blocks: List<RecipeBlock> = emptyList(),
)

/** Путь к картинке-превью: обложка блюда или первое изображение среди блоков. */
val Recipe.previewImagePath: String?
    get() = coverImagePath ?: blocks.filterIsInstance<PictureBlock>().firstOrNull()?.imagePath

/** Общий контракт любого блока рецепта. */
sealed interface RecipeBlock {
    /** Идентификатор блока; [RecipeDefaults.UNKNOWN_ID], если блок ещё не сохранён. */
    val id: Long
}

/** Блок с обычным текстовым указанием. */
data class TextBlock(
    override val id: Long = RecipeDefaults.UNKNOWN_ID,
    /** Текст указания. */
    val text: String,
) : RecipeBlock

/** Блок с изображением, скопированным в приватное хранилище приложения. */
data class PictureBlock(
    override val id: Long = RecipeDefaults.UNKNOWN_ID,
    /** Абсолютный путь к изображению в приватном хранилище приложения. */
    val imagePath: String,
    /** Необязательная подпись к изображению. */
    val caption: String = "",
) : RecipeBlock

/** Блок-список с пунктами, которые можно отмечать во время готовки. */
data class ChecklistBlock(
    override val id: Long = RecipeDefaults.UNKNOWN_ID,
    /** Заголовок списка (например, «Ингредиенты»). */
    val title: String,
    /** Пункты списка в порядке отображения. */
    val items: List<CheckItem> = emptyList(),
) : RecipeBlock

/** Отдельный пункт списка-чеклиста. */
data class CheckItem(
    /** Идентификатор пункта; [RecipeDefaults.UNKNOWN_ID], если пункт ещё не сохранён. */
    val id: Long = RecipeDefaults.UNKNOWN_ID,
    /** Текст пункта. */
    val text: String,
    /** Признак того, что пункт отмечен галочкой. */
    val isChecked: Boolean = false,
)

/** Блок с таймером обратного отсчёта. */
data class TimerBlock(
    override val id: Long = RecipeDefaults.UNKNOWN_ID,
    /** Подпись таймера (например, «Готовка»). */
    val label: String,
    /** Длительность таймера в секундах. */
    val seconds: Long,
) : RecipeBlock
