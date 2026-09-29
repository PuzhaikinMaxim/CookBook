package com.puj.cookbook.recipes.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/** Строка таблицы рецептов. */
@Entity(tableName = "recipes")
data class RecipeEntity(
    /** Первичный ключ; 0 означает авто-генерацию. */
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    /** Название рецепта. */
    val title: String,
    /** Описание рецепта. */
    val description: String,
    /** Путь к большой общей картинке блюда (обложке). */
    val coverImagePath: String? = null,
    /** Момент создания рецепта. */
    val createdAt: Long,
    /** Момент последнего изменения рецепта. */
    val updatedAt: Long,
)

/** Строка таблицы блоков рецепта. */
@Entity(
    tableName = "recipe_blocks",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("recipeId")],
)
data class BlockEntity(
    /** Первичный ключ; 0 означает авто-генерацию. */
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    /** Идентификатор рецепта-владельца. */
    val recipeId: Long,
    /** Позиция блока в рецепте. */
    val position: Int,
    /** Тип блока, см. [RecipeBlockKind]. */
    val kind: String,
    /** Текст для текстового блока. */
    val text: String? = null,
    /** Путь к изображению для блока-картинки. */
    val imagePath: String? = null,
    /** Подпись для блока-картинки. */
    val caption: String? = null,
    /** Заголовок чеклиста или подпись таймера. */
    val label: String? = null,
    /** Длительность таймера в секундах. */
    val seconds: Long? = null,
)

/** Строка таблицы пунктов чеклиста. */
@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = BlockEntity::class,
            parentColumns = ["id"],
            childColumns = ["blockId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("blockId")],
)
data class CheckItemEntity(
    /** Первичный ключ; 0 означает авто-генерацию. */
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    /** Идентификатор блока-владельца. */
    val blockId: Long,
    /** Позиция пункта в чеклисте. */
    val position: Int,
    /** Текст пункта. */
    val text: String,
    /** Признак отмеченного пункта. */
    val isChecked: Boolean,
)

/** Блок вместе с его пунктами чеклиста. */
data class BlockWithItems(
    /** Собственные поля блока. */
    @Embedded val block: BlockEntity,
    /** Пункты чеклиста, относящиеся к блоку. */
    @Relation(
        parentColumn = "id",
        entityColumn = "blockId",
        entity = CheckItemEntity::class,
    )
    val items: List<CheckItemEntity>,
)

/** Рецепт вместе с его блоками. */
data class RecipeWithBlocks(
    /** Собственные поля рецепта. */
    @Embedded val recipe: RecipeEntity,
    /** Блоки рецепта. */
    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId",
        entity = BlockEntity::class,
    )
    val blocks: List<BlockWithItems>,
)
