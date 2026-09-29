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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** ViewModel режима пошаговой готовки: хранит текущий шаг и состояние таймеров. */
@HiltViewModel
class CookAlongViewModel @Inject constructor(
    private val repository: RecipeRepository,
    private val timerController: CookingTimerController,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** Идентификатор рецепта, взятого для пошаговой готовки. */
    val recipeId: Long = savedStateHandle.get<Long>(RecipeNavArgs.RECIPE_ID) ?: RecipeDefaults.UNKNOWN_ID

    /** Поток рецепта, по шагам которого идёт готовка. */
    val recipe: StateFlow<Recipe?> = repository.observeById(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Состояния всех таймеров, доступных на экране готовки. */
    val timerStates: StateFlow<Map<String, CookingTimer>> = timerController.timers

    /** Индекс текущего шага готовки. */
    private val _stepIndex = MutableStateFlow(0)
    val stepIndex: StateFlow<Int> = _stepIndex.asStateFlow()

    /** Переходит к следующему шагу, не выходя за пределы [stepCount]. */
    fun next(stepCount: Int) {
        if (stepCount <= 0) return
        _stepIndex.update { (it + 1).coerceAtMost(stepCount - 1) }
    }

    /** Возвращается к предыдущему шагу, не уходя раньше первого. */
    fun previous() {
        _stepIndex.update { (it - 1).coerceAtLeast(0) }
    }

    /** Переходит к шагу с индексом [index]. */
    fun goTo(index: Int) {
        _stepIndex.value = index.coerceAtLeast(0)
    }

    /** Меняет отметку пункта списка с идентификатором [itemId] на [checked]. */
    fun setCheckItemChecked(itemId: Long, checked: Boolean) {
        viewModelScope.launch { repository.setCheckItemChecked(itemId, checked) }
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
