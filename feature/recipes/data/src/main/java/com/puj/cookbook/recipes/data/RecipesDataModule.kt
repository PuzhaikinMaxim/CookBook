package com.puj.cookbook.recipes.data

import android.content.Context
import androidx.room.Room
import com.puj.cookbook.recipes.data.local.CookBookDatabase
import com.puj.cookbook.recipes.domain.RecipeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt-привязки для слоя хранения рецептов. */
@Module
@InstallIn(SingletonComponent::class)
object RecipesDataModule {

    /** Создаёт singleton-экземпляр базы данных Room. */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CookBookDatabase =
        Room.databaseBuilder(
            context,
            CookBookDatabase::class.java,
            CookBookDatabase.DATABASE_NAME,
        ).build()

    /** Предоставляет реализацию [RecipeRepository] поверх базы данных. */
    @Provides
    @Singleton
    fun provideRecipeRepository(database: CookBookDatabase): RecipeRepository =
        RoomRecipeRepository(database)
}
