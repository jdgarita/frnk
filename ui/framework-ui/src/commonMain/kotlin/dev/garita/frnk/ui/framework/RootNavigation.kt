package dev.garita.frnk.ui.framework

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun Navigation(
    rootScreen: @Composable (navigateToSettings: () -> Unit) -> Unit,
    settingsScreen: @Composable (navigateBack: () -> Unit) -> Unit
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = InternalScreen.Root
    ) {
        composable<InternalScreen.Root> {
            rootScreen { navController.navigate(InternalScreen.Settings) }
        }
        composable<InternalScreen.Settings> {
            settingsScreen { navController.navigateUp() }
        }
    }
}