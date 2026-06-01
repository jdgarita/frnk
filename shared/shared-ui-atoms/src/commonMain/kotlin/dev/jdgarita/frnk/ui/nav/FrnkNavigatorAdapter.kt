package dev.jdgarita.frnk.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.navOptions

/**
 * Adapt a host-owned [NavController] to the Compose-free [FrnkNavigator] abstraction, so MVI effect
 * handlers (and other non-Compose code) can drive navigation. Remembered against [navController] so
 * the same adapter is reused across recompositions.
 *
 * Route the toolkit's one-shot navigation effects into this from a single `EffectCollector` above
 * the `FrnkNavHost` — the effect channel is single-consumer, so collect it in exactly one place.
 */
@Composable
fun rememberFrnkNavigator(navController: NavController): FrnkNavigator =
    remember(navController) { NavControllerFrnkNavigator(navController) }

private class NavControllerFrnkNavigator(
    private val navController: NavController,
) : FrnkNavigator {
    override fun navigate(route: Any) {
        navController.navigate(route)
    }

    override fun navigate(
        route: Any,
        options: FrnkNavOptions,
    ) {
        val navOptions =
            navOptions {
                options.popUpTo?.let { popUpTo ->
                    popUpTo(popUpTo.route) {
                        inclusive = popUpTo.inclusive
                        saveState = popUpTo.saveState
                    }
                }
                launchSingleTop = options.launchSingleTop
                restoreState = options.restoreState
            }
        navController.navigate(route, navOptions)
    }

    override fun navigateUp(): Boolean = navController.navigateUp()

    override fun popBackStack(): Boolean = navController.popBackStack()
}
