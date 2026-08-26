package com.discflux.medialist.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.discflux.medialist.domain.model.MediaCategory
import com.discflux.medialist.ui.addmedia.AddMediaScreen
import com.discflux.medialist.ui.dashboard.DashboardScreen
import com.discflux.medialist.ui.detail.DetailScreen
import com.discflux.medialist.ui.login.LoginScreen
import com.discflux.medialist.ui.medialist.MediaListScreen
import com.discflux.medialist.ui.roulette.RouletteScreen

@Composable
fun MediaHubNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = hiltViewModel(),
                onLoginSuccess = {
                    Log.d("NavGraph", "Login success - navigating to Dashboard")
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = hiltViewModel(),
                onNavigateToMediaList = { category -> 
                    navController.navigate(Screen.MediaList.createRoute(category?.name)) 
                },
                onNavigateToRoulette = { navController.navigate(Screen.Roulette.route) },
                onNavigateToDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }
        composable(
            route = Screen.MediaList.route,
            arguments = listOf(
                navArgument("category") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("category")
            val initialCategory = categoryName?.let { MediaCategory.valueOf(it) }
            
            MediaListScreen(
                viewModel = hiltViewModel(),
                initialCategory = initialCategory,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) },
                onNavigateToAddMedia = { category -> 
                    navController.navigate(Screen.AddMedia.createRoute(category.name))
                }
            )
        }
        composable(Screen.Detail.route) { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")?.toLong() ?: -1L
            DetailScreen(
                mediaId = mediaId,
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Roulette.route) {
            RouletteScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate(Screen.Detail.createRoute(id)) }
            )
        }
        composable(
            route = Screen.AddMedia.route,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("category") ?: ""
            val category = MediaCategory.valueOf(categoryName)
            
            AddMediaScreen(
                category = category,
                viewModel = hiltViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
