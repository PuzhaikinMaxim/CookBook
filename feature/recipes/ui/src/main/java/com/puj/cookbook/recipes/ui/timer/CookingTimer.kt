package com.puj.cookbook.recipes.ui.timer

/** Неизменяемый снимок одного обратного отсчёта, ключ — стабильная строка (например, "recipeId:blockId"). */
data class CookingTimer(
    /** Стабильный ключ таймера. */
    val key: String,
    /** Подпись таймера. */
    val label: String,
    /** Исходная длительность в секундах. */
    val totalSeconds: Long,
    /** Оставшееся время в секундах. */
    val remainingSeconds: Long,
    /** Идёт ли отсчёт в данный момент. */
    val isRunning: Boolean,
    /** Достиг ли таймер нуля. */
    val isFinished: Boolean,
) {
    /** Доля оставшегося времени от исходного (от 0 до 1). */
    val progress: Float
        get() = if (totalSeconds <= 0L) 0f else (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)
}
