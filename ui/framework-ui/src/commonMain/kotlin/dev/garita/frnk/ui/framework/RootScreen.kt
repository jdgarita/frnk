package dev.garita.frnk.ui.framework

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun RootScreen(
    navigateToSettings: () -> Unit,
    navigationBar: @Composable (navController: NavHostController) -> Unit,
    bottomNavigationGraph: @Composable (navController: NavHostController, paddingValues: PaddingValues) -> Unit
) {
    val navController = rememberNavController()
    val backstackEntry by navController.currentBackStackEntryAsState()
    val currentDestinationRoute by remember {
        derivedStateOf {
            backstackEntry?.destination?.route
        }
    }
    val isTopBarVisible by remember {
        derivedStateOf {
            currentDestinationRoute?.contains("Details") == false
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = isTopBarVisible,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = currentDestinationRoute ?: "No Title"
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
            }
        },
        bottomBar = {
            navigationBar(navController)
        }
    ) { padding ->
        val animatedTopPadding by animateDpAsState(
            targetValue = if (isTopBarVisible) padding.calculateTopPadding() else 0.dp,
            animationSpec = tween(durationMillis = 300)
        )
        val modifiedPadding = PaddingValues(
            start = padding.calculateStartPadding(LocalLayoutDirection.current),
            top = animatedTopPadding,
            end = padding.calculateEndPadding(LocalLayoutDirection.current),
            bottom = padding.calculateBottomPadding()
        )
        bottomNavigationGraph(navController, modifiedPadding)
    }
}

private fun checkIfItemSelected(
    bottomBarDestination: String?,
    currentDestinationRoute: String?
): Boolean {
    return if ((currentDestinationRoute?.contains("Home") == true || currentDestinationRoute?.contains("Details") == true) && bottomBarDestination?.contains(
            "Home"
        ) == true
    ) true
    else if (currentDestinationRoute?.contains(bottomBarDestination.toString()) == true) true
    else false
}