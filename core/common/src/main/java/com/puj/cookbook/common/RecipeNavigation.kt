package com.puj.cookbook.common

/** Ключи аргументов навигации, общие для графа рецептов и ViewModel-ей фич. */
object RecipeNavArgs {
    /** Имя аргумента с идентификатором рецепта. */
    const val RECIPE_ID = "recipeId"
}

/**
 * Маршруты Compose Navigation для фичи рецептов. Паттерны и билдеры собраны здесь,
 * чтобы не дублировать строки маршрутов между модулем app и модулями фич.
 */
object RecipeRoutes {
    /** Маршрут списка рецептов. */
    const val RECIPES = "recipes"

    /** Паттерн маршрута просмотра рецепта. */
    const val RECIPE_DETAIL = "recipe/{${RecipeNavArgs.RECIPE_ID}}"

    /** Паттерн маршрута пошаговой готовки. */
    const val COOK_ALONG = "cook/{${RecipeNavArgs.RECIPE_ID}}"

    /** Паттерн маршрута редактора рецепта. */
    const val RECIPE_EDITOR = "editor?${RecipeNavArgs.RECIPE_ID}={${RecipeNavArgs.RECIPE_ID}}"

    /** Строит маршрут просмотра рецепта по [recipeId]. */
    fun recipeDetail(recipeId: Long): String = "recipe/$recipeId"

    /** Строит маршрут пошаговой готовки по [recipeId]. */
    fun cookAlong(recipeId: Long): String = "cook/$recipeId"

    /** Строит маршрут редактора для рецепта [recipeId] (или нового при 0). */
    fun recipeEditor(recipeId: Long): String = "editor?${RecipeNavArgs.RECIPE_ID}=$recipeId"
}
