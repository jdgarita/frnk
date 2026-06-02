package dev.jdgarita.frnk.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

/**
 * Toolkit wrapper over JetBrains CMP navigation-compose. Thin by design — the toolkit ships the
 * `NavHost` machinery and type-safe destination builder, while the **host owns the back-stack
 * instance** ([rememberFrnkNavController] / its own `NavHostController`) and passes it in. Routes
 * are any `@Serializable` type (e.g. [dev.jdgarita.frnk.ui.nav.ToolkitRoute] members or
 * host-defined routes).
 *
 * ```
 * val navController = rememberFrnkNavController()
 * FrnkNavHost(navController, startRoute = ToolkitRoute.Home) {
 *     frnkComposable<ToolkitRoute.Home> { HomeScreen() }
 *     frnkComposable<Detail> { route -> DetailScreen(route.id) }
 * }
 * ```
 */
@Composable
fun FrnkNavHost(
    navController: NavHostController,
    startRoute: Any,
    modifier: Modifier = Modifier,
    builder: NavGraphBuilder.() -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = modifier,
        builder = builder,
    )
}

/**
 * Host-owned navigation controller for a [FrnkNavHost]. Wraps `rememberNavController()`; the host
 * holds this instance, so the back stack is host-owned even though the toolkit ships the machinery.
 */
@Composable
fun rememberFrnkNavController(): NavHostController = rememberNavController()

/**
 * Register a type-safe destination for the `@Serializable` route [T]. Wraps navigation-compose's
 * reified `composable<T>` and decodes the back-stack entry's arguments back into [T] via `toRoute`,
 * handing the typed route to [content] — so destinations read arguments type-safely instead of
 * pulling `String` keys out of a bundle.
 */
inline fun <reified T : Any> NavGraphBuilder.frnkComposable(noinline content: @Composable (route: T) -> Unit) {
    composable<T> { entry -> content(entry.toRoute<T>()) }
}
