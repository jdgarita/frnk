package dev.jdgarita.frnk.ui.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.jdgarita.frnk.ui.nav.FrnkRootRoute
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.AppearanceController
import dev.jdgarita.frnk.ui.theme.FrnkTheme
import dev.jdgarita.frnk.ui.theme.LocalAppearanceController
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module

/**
 * The toolkit's app root — owns only the app chrome and hands the navigation graph to the host. The host
 * registers its own root destinations (onboarding / tab shell / paywall) and wires the nested tab
 * navigation itself, keeping full control over the nav graph (nothing is auto-mounted).
 *
 * It provides the [AppearanceController]-driven light/dark [FrnkTheme], system-bar appearance, the
 * `AppScaffold` window chrome, and a single **root** [androidx.navigation3.ui.NavDisplay] seeded at
 * [FrnkRootRoute.Onboarding]. The graph itself comes from the host:
 *  - [onSavedStateConfiguration] supplies the root `SavedStateConfiguration` (typically
 *    [dev.jdgarita.frnk.ui.nav.frnkRootNavConfig]) that persists/restores the root back stack.
 *  - [onNavigationModule] receives the root [NavBackStack] and returns a **Koin navigation module**
 *    (built with `org.koin.dsl.navigation3.navigation<Route> { … }`), loaded via `loadKoinModules`, that
 *    registers the root destinations (onboarding, the tabbed shell, paywall, …). Destinations are
 *    resolved by the display's `koinEntryProvider()`.
 *
 * No batteries are wired here — the host registers its own root destinations and owns the nested tab
 * navigation (e.g. via [FrnkRootRoute.Tab] hosting a nested back stack built with
 * [dev.jdgarita.frnk.ui.nav.frnkNestedNavConfig]). Assumes `initializeFrnk(...)` has run (the
 * [AppearanceController] is resolved from Koin).
 *
 * @param onSavedStateConfiguration supplies the root back stack's `SavedStateConfiguration`.
 * @param onNavigationModule returns the Koin navigation module registering the root destinations, given
 *   the root back stack to drive from effect handlers.
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun FrnkApp(
    onSavedStateConfiguration: () -> SavedStateConfiguration,
    onNavigationModule: (backStack: NavBackStack<NavKey>) -> Module
) {
    val appearanceController: AppearanceController = koinInject()

    CompositionLocalProvider(
        LocalAppearanceController provides appearanceController
    ) {
        val darkTheme =
            when (appearanceController.appearance) {
                Appearance.Light -> false
                Appearance.Dark -> true
                Appearance.System -> isSystemInDarkTheme()
            }

        ApplySystemBarAppearance(darkTheme = darkTheme)

        FrnkTheme {
            AppScaffold {
                val initialRoute = FrnkRootRoute.Onboarding

                val backStack =
                    rememberNavBackStack(
                        configuration = onSavedStateConfiguration(),
                        elements = arrayOf(initialRoute)
                    )

                remember(backStack) {
                    loadKoinModules(
                        module = onNavigationModule(backStack)
                    )
                }

                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.back() },
                    entryProvider = koinEntryProvider(),
                    entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                    transitionSpec = { defaultEnterTransition() },
                    popTransitionSpec = { defaultExitTransition() },
                    predictivePopTransitionSpec = { defaultExitTransition() }
                )
            }
        }
    }
}