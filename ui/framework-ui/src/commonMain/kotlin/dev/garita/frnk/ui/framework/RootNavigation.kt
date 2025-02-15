package dev.garita.frnk.ui.framework

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun RootNavigationGraph(
    root: @Composable (navigateToSettings: () -> Unit) -> Unit,
    settings: @Composable (navigateBack: () -> Unit) -> Unit
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = InternalScreen.Root
    ) {
        composable<InternalScreen.Root> {
            root { navController.navigate(InternalScreen.Settings) }
        }
        composable<InternalScreen.Settings> {
            settings { navController.navigateUp() }
//            SettingsScreen(
//                navigateBack = {
//                    navController.navigateUp()
//                }
//            )
        }
    }
}