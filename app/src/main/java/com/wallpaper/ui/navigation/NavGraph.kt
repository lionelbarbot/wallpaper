package com.wallpaper.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wallpaper.ui.screens.*

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToFolder = { folderId ->
                    navController.navigate(Screen.FolderDetail(folderId).createRoute(folderId))
                },
                onNavigateToEditFolder = { folderId ->
                    val route = if (folderId != null) {
                        Screen.EditFolder(folderId).createRoute(folderId)
                    } else {
                        Screen.EditFolder(null).createRoute(null)
                    }
                    navController.navigate(route)
                }
            )
        }
        
        composable(
            route = Screen.FolderDetail(0).route,
            arguments = listOf(navArgument("folderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getLong("folderId") ?: 0L
            FolderDetailScreen(
                folderId = folderId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditImage = { imageId, imagePath ->
                    navController.navigate(Screen.EditImage(imageId).createRoute(imageId))
                }
            )
        }
        
        composable(
            route = Screen.EditFolder(null).route.replace("{folderId}", "new")
        ) {
            EditFolderScreen(
                folderId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.EditFolder(0).route,
            arguments = listOf(navArgument("folderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val folderId = backStackEntry.arguments?.getLong("folderId")
            EditFolderScreen(
                folderId = folderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.EditImage(0).route,
            arguments = listOf(navArgument("imageId") { type = NavType.LongType })
        ) { backStackEntry ->
            val imageId = backStackEntry.arguments?.getLong("imageId") ?: 0L
            ImageEditorScreen(
                imageId = imageId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

