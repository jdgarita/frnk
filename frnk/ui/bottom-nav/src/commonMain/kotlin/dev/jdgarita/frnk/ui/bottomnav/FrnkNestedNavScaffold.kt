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
import dev.jdgarita.frnk.ui.nav.FrnkNavTab
import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.nav.navigateTo
import dev.jdgarita.frnk.ui.scaffolds.LocalFrnkBottomBarInset
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module

/**
 * A **flexible multiple-back-stack tabbed scaffold** — the toolkit's bottom-bar navigation for a host
 * that wants its **own** set of tabs (any count, any roots, all destinations host-supplied) rather than
 * the fixed `Home · feature · Settings` shape + built-in Home/Settings destinations of
 * [FrnkTabbedNavScaffold].
 *
 * **The scaffold owns the navigation plumbing**, so the host stays declarative: it supplies the [tabs],
 * the saved-state config via [onSavedStateConfiguration], and a Koin nav module via
 * [onNestedNavigationModule]; the scaffold derives the bar items + their target routes from [tabs], loads
 * the module against the back stack (`loadKoinModules`), and renders the core:
 *  - a [FrnkNavDisplay] driven by the back stack,
 *  - the platform-adaptive [FrnkBottomFloatingBar] overlaid above it (so it persists across tab swaps),
 *  - tab switching driven by [FrnkNestedNavViewModel] (the selected tab is VM-owned state, not `remember`),
 *  - the bottom-inset bookkeeping (`LocalFrnkBottomBarInset`) so screens on
 *    `FrnkScreenScaffold` / `FrnkMviScreen` reserve the bar's footprint automatically.
 *
 * Selection state (`items` + `selectedIndex`) lives in [FrnkNestedNavViewModel], not in a `remember*`
 * holder: a tap updates the model's `selectedIndex` and emits a [FrnkNestedNavEffect.Navigate] carrying the
 * tapped tab's root route, which this scaffold applies to the back stack.
 *
 * **Interim:** a single shared back stack drives every tab. True per-tab back stacks (so a tab never loses
 * its nested navigation) and the back-from-a-non-home-tab-root → home convention are the planned follow-up,
 * where the back stacks move into the ViewModel too.
 *
 * [FrnkTabbedNavScaffold] is the batteries-included fixed-three-tab app; reach for this lower-level
 * scaffold when you need a different tab shape and supply every destination yourself.
 *
 * @param tabs the bar tabs, each declared once ([FrnkNavTab]: key + root + icon + label + SF-Symbol);
 *   the bar items and their target routes are derived from this list.
 * @param onSavedStateConfiguration the saved-state config for the back stack (e.g.
 *   `frnkNestedNavConfig(hostRoutes)`).
 * @param onNestedNavigationModule builds the Koin nav module registering the tabs' destinations,
 *   bound to the scaffold-owned back stack.
 */
@OptIn(KoinExperimentalAPI::class, ExperimentalComposeUiApi::class)
@Composable
fun FrnkNestedNavScaffold(
    modifier: Modifier = Modifier,
    tabs: List<FrnkNavTab>,
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

    // The bar items + their target routes are derived once from the host's [tabs], so a host describes each
    // tab exactly once and the bar/routing stay in lockstep (no hardcoded item or index→route lists).
    val navBarItems =
        remember(tabs) {
            tabs.map { tab ->
                FrnkNavBarItemModel(
                    key = tab.key,
                    icon = FrnkIconSource.Vector(tab.icon),
                    iosSystemIcon = tab.iosSystemIcon,
                    label = tab.label,
                    route = tab.root
                )
            }
        }

    FrnkScreen(
        arguments = FrnkNestedNavArguments(items = navBarItems),
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