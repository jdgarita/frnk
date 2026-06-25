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
import dev.jdgarita.frnk.ui.mvi.FrnkScreen
import dev.jdgarita.frnk.ui.nav.FrnkNavDisplay
import dev.jdgarita.frnk.ui.scaffolds.LocalFrnkBottomBarInset
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module

/**
 * The toolkit's **fixed three-tab** bottom-nav scaffold — a `Home · <custom> · Settings` bar over a
 * **multiple-back-stack** tabbed surface. Home and Settings are toolkit-fixed (theme icon tokens,
 * SF-Symbols, and routes [FrnkTabRoute.Home][dev.jdgarita.frnk.ui.nav.FrnkTabRoute.Home] /
 * [FrnkTabRoute.Settings][dev.jdgarita.frnk.ui.nav.FrnkTabRoute.Settings]); the **middle tab is host-provided**
 * via [customTab] (route + icon + SF-Symbol + label).
 *
 * **The scaffold owns the navigation plumbing**, so the host stays declarative: it supplies the middle
 * [customTab] and a Koin nav module via [onNestedNavigationModule]; the scaffold loads the module against
 * the back stack (`loadKoinModules`) and renders the core:
 *  - a [FrnkNavDisplay] driven by the active tab's back stack,
 *  - the platform-adaptive [FrnkBottomFloatingBar] overlaid above it (so it persists across tab swaps),
 *  - the bottom-inset bookkeeping ([LocalFrnkBottomBarInset]) so screens on
 *    `FrnkScreenScaffold` reserves the bar's footprint automatically.
 *
 * **Per-tab back stacks** live in [FrnkNestedNavViewModel] (not in a `remember*` holder): each tab keeps
 * its own nested-navigation history, swapped in/out of the single live `backStack` on tab change. A tap →
 * [FrnkNestedNavIntent.Tap] → the VM switches the active tab (re-tapping the active tab pops it to root);
 * system/predictive back at a tab root from a non-Home tab returns to Home. The host's within-tab
 * navigation drives the same back stack handed to [onNestedNavigationModule] (so `backStack.navigateTo(...)`
 * pushes onto the active tab).
 *
 * **In-memory:** the per-tab stacks survive recomposition + configuration change but not full process death.
 *
 * @param customTab the host-provided middle tab (route + icon + SF-Symbol + label).
 * @param onNestedNavigationModule builds the Koin nav module registering the tabs' destinations
 *   ([FrnkTabRoute.Home][dev.jdgarita.frnk.ui.nav.FrnkTabRoute.Home] / [customTab]'s route /
 *   [FrnkTabRoute.Settings][dev.jdgarita.frnk.ui.nav.FrnkTabRoute.Settings]), bound to the scaffold-owned back stack.
 */
@OptIn(KoinExperimentalAPI::class, ExperimentalComposeUiApi::class)
@Composable
fun FrnkNestedNavScaffold(
    customTab: FrnkCustomTab,
    modifier: Modifier = Modifier,
    onNestedNavigationModule: (backStack: NavBackStack<NavKey>) -> Module
) {
    val viewModel: FrnkNestedNavViewModel = koinViewModel()
    val backStack = viewModel.backStack

    remember(backStack) {
        loadKoinModules(
            modules = listOf(onNestedNavigationModule(backStack))
        )
    }

    // FrnkScreen owns the attach (handing the VM the host's middle tab via FrnkNestedNavArguments) + the
    // lifecycle-aware state collection. The VM emits no effects (navigation is VM-internal), and back is
    // handled by this scaffold rather than FrnkScreen's default handler — hence handleBackPressed = false.
    FrnkScreen(
        viewModel = viewModel,
        arguments = FrnkNestedNavArguments(customTab = customTab),
        handleBackPressed = false,
        onEffect = {}
    ) { state ->
        // Within-tab pops are handled by FrnkNavDisplay's own back. This handler only covers the
        // back-from-a-non-home-tab-root → Home convention: it is enabled exactly when FrnkNavDisplay's back
        // is not (a single-entry active stack) and we're not already on Home.
        BackHandler(enabled = backStack.size <= 1 && state.selectedIndex != HOME_TAB_INDEX) {
            viewModel.send(intent = FrnkNestedNavIntent.Back)
        }

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

private const val HOME_TAB_INDEX = 0