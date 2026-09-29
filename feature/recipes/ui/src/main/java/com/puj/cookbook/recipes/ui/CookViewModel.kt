package com.puj.cookbook.recipes.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puj.cookbook.core.RecipeNavArgs
import com.puj.cookbook.recipes.domain.Recipe
import com.puj.cookbook.recipes.domain.RecipeDefaults
import com.puj.cookbook.recipes.domain.RecipeRepository
import com.puj.cookbook.recipes.domain.TimerBlock
import com.puj.cookbook.recipes.ui.timer.CookingTimer
import com.puj.cookbook.recipes.ui.timer.CookingTimerController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel экрана просмотра рецепта (обзор с возможностью запуска готовки). */
@HiltViewModel
class CookViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val timerController: CookingTimerController,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** Идентификатор открытого рецепта, взятый из аргументов навигации. */
    val recipeId: Long = savedStateHandle.get<Long>(RecipeNavArgs.RECIPE_ID) ?: RecipeDefaults.UNKNOWN_ID

    /** Поток рецепта; null, если рецепт ещё не найден. */
    val recipe: StateFlow<Recipe?> = repository.observeById(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Состояния всех таймеров, доступных на экране. */
    val timerStates: StateFlow<Map<String, CookingTimer>> = timerController.timers

    /** Меняет отметку пункта списка с идентификатором [itemId] на [checked]. */
    fun setCheckItemChecked(itemId: Long, checked: Boolean) {
        viewModelScope.launch { repository.setCheckItemChecked(itemId, checked) }
    }

    /** Регистрирует таймер блока [block] в контроллере, если он ещё не зарегистрирован. */
    fun ensureTimer(block: TimerBlock) {
        timerController.ensure(timerKey(block), block.label, block.seconds)
    }

    /** Запускает таймер блока [block] или ставит его на паузу, если он уже идёт. */
    fun toggleTimer(block: TimerBlock) {
        val key = timerKey(block)
        timerController.ensure(key, block.label, block.seconds)
        if (timerController.timers.value[key]?.isRunning == true) {
            timerController.pause(key)
        } else {
            timerController.start(key)
        }
    }

    /** Сбрасывает таймер блока [block] к исходной длительности. */
    fun resetTimer(block: TimerBlock) {
        timerController.reset(timerKey(block))
    }

    /** Строит стабильный ключ таймера для блока [block]. */
    private fun timerKey(block: TimerBlock): String {
        return CookingTimerController.timerKey(recipeId, block.id)
    }
}
