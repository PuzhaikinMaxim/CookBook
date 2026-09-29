package com.puj.cookbook.recipeeditor.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.puj.cookbook.recipeeditor.domain.RecipeImageStore
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Android-реализация [RecipeImageStore], копирующая выбранные изображения в хранилище приложения. */
class AndroidRecipeImageStore(context: Context) : RecipeImageStore {

    /** Резолвер контента для чтения выбранного Uri. */
    private val resolver: ContentResolver = context.contentResolver

    /** Каталог для копий изображений внутри приватного хранилища приложения. */
    private val targetDir: File = File(context.filesDir, IMAGE_DIRECTORY).apply { mkdirs() }

    override suspend fun import(source: Uri, displayName: String?): String = withContext(Dispatchers.IO) {
        val extension = guessExtension(displayName)
        val target = File(targetDir, "${UUID.randomUUID()}.$extension")
        resolver.openInputStream(source)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Could not open image $source")
        target.absolutePath
    }

    override fun isLocalImage(path: String): Boolean = path.startsWith(targetDir.absolutePath)

    /** Определяет расширение файла по его отображаемому имени [displayName]. */
    private fun guessExtension(displayName: String?): String = when {
        displayName == null -> DEFAULT_EXTENSION
        displayName.substringAfterLast('.', "").isBlank() -> DEFAULT_EXTENSION
        else -> displayName.substringAfterLast('.', DEFAULT_EXTENSION)
    }

    private companion object {
        /** Имя каталога для изображений рецептов. */
        const val IMAGE_DIRECTORY = "recipe_images"

        /** Расширение файла по умолчанию. */
        const val DEFAULT_EXTENSION = "jpg"
    }
}
