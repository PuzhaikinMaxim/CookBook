package com.puj.cookbook.recipes.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puj.cookbook.recipes.domain.Recipe
import com.puj.cookbook.recipes.domain.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** ViewModel экрана со списком всех рецептов. */
@HiltViewModel
class RecipesViewModel @Inject constructor(
    private val repository: RecipeRepository,
) : ViewModel() {

    /** Поток всех рецептов для отображения в списке. */
    val recipes: StateFlow<List<Recipe>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Удаляет рецепт по идентификатору [id]. */
    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}
