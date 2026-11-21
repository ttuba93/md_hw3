package com.example.moviewatchlist.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.moviewatchlist.ui.detail.DetailScreen
import com.example.moviewatchlist.ui.edit.EditScreen
import com.example.moviewatchlist.ui.history.HistoryScreen
import com.example.moviewatchlist.ui.list.WatchlistScreen
import com.example.moviewatchlist.ui.stats.StatsScreen

object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{movieId}"
    const val EDIT = "edit?movieId={movieId}"
    const val HISTORY = "history"
    const val STATS = "stats"

    fun detail(id: Long) = "detail/$id"
    fun edit(id: Long = -1L) = "edit?movieId=$id"
}


private const val FLASH_KEY = "flash"

@Composable
fun WatchlistNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LIST) {

        composable(Routes.LIST) { entry ->
            val flash by entry.savedStateHandle.getStateFlow<String?>(FLASH_KEY, null).collectAsStateWithLifecycle()
            WatchlistScreen(
                onOpenMovie = { navController.navigate(Routes.detail(it)) },
                onAdd = { navController.navigate(Routes.edit()) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenStats = { navController.navigate(Routes.STATS) },
                flash = flash,
                onFlashShown = { entry.savedStateHandle[FLASH_KEY] = null }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("movieId") { type = NavType.LongType })
        ) { entry ->
            val flash by entry.savedStateHandle.getStateFlow<String?>(FLASH_KEY, null).collectAsStateWithLifecycle()
            DetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.edit(it)) },
                flash = flash,
                onFlashShown = { entry.savedStateHandle[FLASH_KEY] = null }
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(
                navArgument("movieId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            EditScreen(
                onBack = { navController.popBackStack() },
                onSaved = { message ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(FLASH_KEY, message)
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HISTORY) { HistoryScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.STATS) { StatsScreen(onBack = { navController.popBackStack() }) }
    }
}
