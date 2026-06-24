package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.jdgarita.frnk.ui.mvi.FrnkScreen
import dev.jdgarita.frnk.ui.nav.FrnkNavDisplay
import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.nav.navigateTo
import dev.jdgarita.frnk.ui.scaffolds.LocalFrnkBottomBarInset
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.iconNavComponent
import dev.jdgarita.frnk.ui.theme.iconNavHome
import dev.jdgarita.frnk.ui.theme.iconNavSettings
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module

/**
 * The toolkit's **fixed three-tab** bottom-nav scaffold — a `Home · Components · Settings` bar over a
 * multiple-back-stack tabbed surface. The three tabs (labels, theme icon tokens, SF-Symbols, and routes —
 * [FrnkRoute.Home], [FrnkRoute.Custom] `"Components"`, [FrnkRoute.Settings]) are defined **inside the
 * scaffold**; the host supplies only the destinations behind those routes.
 *
 * **The scaffold owns the navigation plumbing**, so the host stays declarative: it supplies the saved-state
 * config via [onSavedStateConfiguration] and a Koin nav module via [onNestedNavigationModule]; the scaffold
 * loads the module against the back stack (`loadKoinModules`) and renders the core:
 *  - a [FrnkNavDisplay] driven by the back stack,
 *  - the platform-adaptive [FrnkBottomFloatingBar] overlaid above it (so it persists across tab swaps),
 *  - tab switching driven by [FrnkNestedNavViewModel] (the selected tab is VM-owned state, not `remember`),
 *  - the bottom-inset bookkeeping (`LocalFrnkBottomBarInset`) so screens on
 *    `FrnkScreenScaffold` / `FrnkMviScreen` reserve the bar's footprint automatically.
 *
 * Selection state (`items` + `selectedIndex`) lives in [FrnkNestedNavViewModel], not in a `remember*`
 * holder: a tap updates the model's `selectedIndex` and emits a [FrnkNestedNavEffect.Navigate] carrying the
 * tapped tab's route, which this scaffold applies to the back stack.
 *
 * **Interim:** a single shared back stack drives every tab. True per-tab back stacks (so a tab never loses
 * its nested navigation) and the back-from-a-non-home-tab-root → home convention are the planned follow-up,
 * where the back stacks move into the ViewModel too.
 *
 * @param onSavedStateConfiguration the saved-state config for the back stack (e.g.
 *   `frnkNestedNavConfig(hostRoutes)`).
 * @param onNestedNavigationModule builds the Koin nav module registering the tabs' destinations
 *   ([FrnkRoute.Home] / [FrnkRoute.Custom] `"Components"` / [FrnkRoute.Settings]), bound to the
 *   scaffold-owned back stack.
 */
@OptIn(KoinExperimentalAPI::class, ExperimentalComposeUiApi::class)
@Composable
fun FrnkNestedNavScaffold(
    modifier: Modifier = Modifier,
    onSavedStateConfiguration: () -> SavedStateConfiguration,
    onNestedNavigationModule: (backStack: NavBackStack<NavKey>) -> Module
) {
    val viewModel: FrnkNestedNavViewModel = koinViewModel()

    val backStack =
        rememberNavBackStack(
            configuration = onSavedStateConfiguration(),
            elements = arrayOf(FrnkRoute.Home)
        )

    remember(backStack) {
        loadKoinModules(
            modules =
                listOf(
                    onNestedNavigationModule(backStack)
                )
        )
    }

    // Interim single-stack back convention: pop the current screen. The back-from-a-non-home-tab-root → home
    // convention lands with the per-tab back-stacks follow-up.
    BackHandler(enabled = true) {
        backStack.back()
    }

    FrnkScreen(
        arguments =
            FrnkNestedNavArguments(
                items =
                    listOf(
                        FrnkNavBarItemModel(
                            key = "Home",
                            icon = FrnkIconSource.Token(iconNavHome),
                            iosSystemIcon = "house",
                            label = "Home",
                            route = FrnkRoute.Home
                        ),
                        FrnkNavBarItemModel(
                            key = "Components",
                            icon = FrnkIconSource.Token(iconNavComponent),
                            iosSystemIcon = "square.grid.2x2",
                            label = "Components",
                            route = FrnkRoute.Custom("Components")
                        ),
                        FrnkNavBarItemModel(
                            key = "Settings",
                            icon = FrnkIconSource.Token(iconNavSettings),
                            iosSystemIcon = "gearshape",
                            label = "Settings",
                            route = FrnkRoute.Settings
                        )
                    )
            ),
        viewModel = viewModel,
        onEffect = { uiEffect ->
            when (uiEffect) {
                is FrnkNestedNavEffect.Navigate -> backStack.navigateTo(uiEffect.route)
            }
        }
    ) { state ->

        // The scaffold owns the back stack + loads the host's nested module against it, so the host supplies
        // only the saved-state config + a module factory (and never touches the back stack).

        val reservedHeight = FrnkNavBarDefaults.reservedHeight

        Box(modifier = modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalFrnkBottomBarInset provides reservedHeight) {
                FrnkNavDisplay(
                    backStack = backStack,
                    modifier = Modifier.fillMaxSize()
                )
            }

            FrnkBottomFloatingBar(
                items = state.items,
                selectedIndex = state.selectedIndex,
                onItemSelected = { index ->
                    viewModel.send(
                        intent =
                            FrnkNestedNavIntent.Tap(
                                index = index
                            )
                    )
                },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            )
        }
    }
}