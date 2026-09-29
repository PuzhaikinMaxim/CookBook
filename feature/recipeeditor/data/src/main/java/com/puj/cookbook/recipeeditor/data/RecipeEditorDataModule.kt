package com.puj.cookbook.recipeeditor.data

import android.content.Context
import com.puj.cookbook.recipeeditor.domain.RecipeImageStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt-привязка для хранилища изображений, используемого при составлении рецепта. */
@Module
@InstallIn(SingletonComponent::class)
object RecipeEditorDataModule {

    /** Предоставляет Android-реализацию [RecipeImageStore]. */
    @Provides
    @Singleton
    fun provideRecipeImageStore(@ApplicationContext context: Context): RecipeImageStore =
        AndroidRecipeImageStore(context)
}
