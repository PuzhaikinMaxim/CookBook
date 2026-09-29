package com.puj.cookbook.recipes.data.local

/** Сохраняемые значения-дискриминаторы для поля [BlockEntity.kind]. */
object RecipeBlockKind {
    /** Текстовый блок. */
    const val TEXT = "TEXT"

    /** Блок-изображение. */
    const val PICTURE = "PICTURE"

    /** Блок-чеклист. */
    const val CHECKLIST = "CHECKLIST"

    /** Блок-таймер. */
    const val TIMER = "TIMER"
}
