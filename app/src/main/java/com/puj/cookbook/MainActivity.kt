package com.puj.cookbook

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.puj.cookbook.core.CookBookScaffold
import com.puj.cookbook.core.CookBookSplash
import com.puj.cookbook.core.CookBookTheme
import com.puj.cookbook.core.RecipeNavArgs
import com.puj.cookbook.core.RecipeRoutes
import com.puj.cookbook.recipes.domain.RecipeDefaults
import com.puj.cookbook.recipes.ui.CookAlongScreen
import com.puj.cookbook.recipes.ui.CookScreen
import com.puj.cookbook.recipes.ui.RecipesScreen
import com.puj.cookbook.recipeeditor.ui.RecipeEditorScreen
import dagger.hilt.android.AndroidEntryPoint

/** Главная Activity с единственным Compose-графом навигации приложения. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CookBookTheme {
                CookBookApp()
            }
        }
    }
}

/**
 * Корневой composable приложения: показывает анимированную заставку, затем плавно
 * переходит к графу навигации.
 */
@Composable
fun CookBookApp() {
    var showSplash by rememberSaveable { mutableStateOf(true) }

    Crossfade(
        targetState = showSplash,
        animationSpec = tween(durationMillis = 500),
        label = "splashTransition",
    ) { isSplash ->
        if (isSplash) {
            CookBookSplash(onFinished = { showSplash = false })
        } else {
            MainNavigation()
        }
    }
}

/** Граф навигации между списком рецептов, готовкой и редактором. */
@Composable
private fun MainNavigation() {
    RequestNotificationPermission()
    val navController = rememberNavController()

    CookBookScaffold {
        NavHost(
            navController = navController,
            startDestination = RecipeRoutes.RECIPES,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(RecipeRoutes.RECIPES) {
                RecipesScreen(
                    onOpenRecipe = { id -> navController.navigate(RecipeRoutes.recipeDetail(id)) },
                    onAddRecipe = {
                        navController.navigate(RecipeRoutes.recipeEditor(RecipeDefaults.UNKNOWN_ID))
                    },
                )
            }
            composable(
                route = RecipeRoutes.RECIPE_DETAIL,
                arguments = listOf(navArgument(RecipeNavArgs.RECIPE_ID) { type = NavType.LongType }),
            ) {
                CookScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(RecipeRoutes.recipeEditor(id)) },
                    onStartCooking = { id -> navController.navigate(RecipeRoutes.cookAlong(id)) },
                )
            }
            composable(
                route = RecipeRoutes.COOK_ALONG,
                arguments = listOf(navArgument(RecipeNavArgs.RECIPE_ID) { type = NavType.LongType }),
            ) {
                CookAlongScreen(
                    onBack = { navController.popBackStack() },
                    onFinish = { navController.popBackStack() },
                )
            }
            composable(
                route = RecipeRoutes.RECIPE_EDITOR,
                arguments = listOf(
                    navArgument(RecipeNavArgs.RECIPE_ID) {
                        type = NavType.LongType
                        defaultValue = RecipeDefaults.UNKNOWN_ID
                    }
                ),
            ) {
                RecipeEditorScreen(
                    onSaved = { id ->
                        navController.popBackStack(RecipeRoutes.RECIPES, inclusive = false)
                        navController.navigate(RecipeRoutes.recipeDetail(id))
                    },
                    onCancel = { navController.popBackStack() },
                )
            }
        }
    }
}

/** Однократно запрашивает разрешение на уведомления, чтобы таймеры могли оповещать о завершении. */
@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {},
    )
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
