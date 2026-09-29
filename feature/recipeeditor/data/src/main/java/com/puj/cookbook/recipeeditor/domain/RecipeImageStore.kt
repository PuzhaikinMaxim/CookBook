package com.puj.cookbook.recipeeditor.domain

import android.net.Uri

/** Копирует выбранное изображение (Uri из системного выбора фото) в приватное хранилище приложения. */
interface RecipeImageStore {
    /** Импортирует изображение [source] и возвращает абсолютный путь к его копии. */
    suspend fun import(source: Uri, displayName: String?): String

    /** Проверяет, что [path] указывает на изображение внутри хранилища приложения. */
    fun isLocalImage(path: String): Boolean
}
