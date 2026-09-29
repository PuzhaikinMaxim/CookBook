package com.puj.cookbook.recipes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/** База данных приложения CookBook. */
@Database(
    entities = [RecipeEntity::class, BlockEntity::class, CheckItemEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class CookBookDatabase : RoomDatabase() {
    /** DAO для работы с рецептами. */
    abstract fun recipeDao(): RecipeDao

    companion object {
        /** Имя файла базы данных. */
        const val DATABASE_NAME = "cookbook.db"
    }
}
