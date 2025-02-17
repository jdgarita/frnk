package dev.garita.frnk.ui.framework

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(
    navigateToSettings: () -> Unit,
    bottomBarDestinationList: List<BottomBarDestination>,
    bottomNavigationGraph: @Composable (navController: NavHostController, paddingValues: PaddingValues) -> Unit
) {
    val navController = rememberNavController()
    val backstackEntry by navController.currentBackStackEntryAsState()
    val currentDestinationRoute by remember {
        derivedStateOf {
            backstackEntry?.destination?.route
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = bottomBarDestinationList.labelBasedOnRoute(currentDestinationRoute)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navigateToSettings()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomBarDestinationList.forEach { destination ->
                    NavigationBarItem(
                        selected = checkIfItemSelected(
                            currentDestinationRoute = currentDestinationRoute,
                            bottomBarDestination = destination.label
                        ),
                        label = { Text(destination.label) },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            navController.navigate(destination.screen) {
                                popUpTo(navController.graph.findStartDestination().route!!) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        bottomNavigationGraph(navController, innerPadding)
    }
}

private fun checkIfItemSelected(
    bottomBarDestination: String?,
    currentDestinationRoute: String?
): Boolean = when {
    (
        currentDestinationRoute?.contains("Home") == true ||
            currentDestinationRoute?.contains("Details") == true
        ) && bottomBarDestination?.contains(
        "Home"
    ) == true -> true

    currentDestinationRoute?.contains(bottomBarDestination.toString()) == true -> true
    else -> false
}

private fun List<BottomBarDestination>.labelBasedOnRoute(route: String?): String =
    this.find { route?.contains(it.label) == true }?.label ?: "Invalid title"