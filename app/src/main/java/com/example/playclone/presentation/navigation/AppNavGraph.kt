package com.example.playclone.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.playclone.di.AppContainer
import com.example.playclone.di.ViewModelFactory
import com.example.playclone.presentation.screens.addapp.AddAppScreen
import com.example.playclone.presentation.screens.detail.AppDetailScreen
import com.example.playclone.presentation.screens.home.HomeScreen
import com.example.playclone.presentation.screens.search.SearchScreen
import com.example.playclone.presentation.viewmodel.AppsViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object AddApp : Screen("add_app")
    object AppDetail : Screen("app_detail/{appId}") {
        fun createRoute(appId: String) = "app_detail/$appId"
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    val viewModelFactory = ViewModelFactory(appContainer)
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            val viewModel: AppsViewModel = viewModel(factory = viewModelFactory)
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToAddApp = {
                    navController.navigate(Screen.AddApp.route)
                },
                onAppClick = { appId ->
                    navController.navigate(Screen.AppDetail.createRoute(appId))
                }
            )
        }
        
        composable(Screen.Search.route) {
            val viewModel: AppsViewModel = viewModel(factory = viewModelFactory)
            SearchScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAppClick = { appId ->
                    navController.navigate(Screen.AppDetail.createRoute(appId))
                }
            )
        }
        
        composable(Screen.AddApp.route) {
            val viewModel: AppsViewModel = viewModel(factory = viewModelFactory)
            AddAppScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.AppDetail.route,
            arguments = listOf(
                navArgument("appId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getString("appId") ?: return@composable
            val viewModel: AppsViewModel = viewModel(factory = viewModelFactory)
            AppDetailScreen(
                appId = appId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
