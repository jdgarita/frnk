package dev.jdgarita.frnk.ui.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

/**
 * Create the host-owned Navigation3 back stack, seeded with [start] and configured with [configuration]
 * (build it via `frnkNestedNavConfig(hostRoutes = …)` for a tab/nested stack, or `frnkRootNavConfig` for the
 * root stack). `rememberSaveable`-backed, so the stack survives configuration change and process death.
 *
 * ```
 * val backStack = rememberFrnkNavBackStack(appNavConfig, startRoute)
 * FrnkNavDisplay(backStack)
 * ```
 */
@Composable
fun rememberFrnkNavBackStack(
    configuration: SavedStateConfiguration,
    vararg start: NavKey
): NavBackStack<NavKey> = rememberNavBackStack(configuration = configuration, elements = start)

/**
 * The toolkit's Navigation3 host: a thin wrapper over nav3's `NavDisplay` that bakes in the two standard
 * entry decorators (saveable-state-holder + ViewModel-store, so each entry keeps its `rememberSaveable`
 * state and its scoped `ViewModel`s) and the toolkit's slide [frnkEnterTransition]/[frnkExitTransition].
 *
 * The **host owns the back stack** (a `NavBackStack<NavKey>` from [rememberFrnkNavBackStack]). By default
 * routes resolve through Koin's [koinEntryProvider] (pair with the `navigation<Route> { … }` DSL); pass a
 * local `entryProvider { entry<Route> { … } }` to register destinations inline instead (the demo does this
 * because its screens share one host-scoped ViewModel). Transitions are overridable for hosts that want a
 * different motion; the back-stack mutation helpers ([navigateTo], [back], [clearAndNavigateTo]) drive it.
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun FrnkNavDisplay(
    backStack: NavBackStack<NavKey>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { backStack.back() },
    entryProvider: (NavKey) -> NavEntry<NavKey> = koinEntryProvider(),
    transitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = { frnkEnterTransition() },
    popTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = { frnkExitTransition() },
    predictivePopTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform = { frnkExitTransition() }
) {
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { onBack() },
        entryProvider = entryProvider,
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
        transitionSpec = transitionSpec,
        popTransitionSpec = popTransitionSpec,
        predictivePopTransitionSpec = predictivePopTransitionSpec
    )
}