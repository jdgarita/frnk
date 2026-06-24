package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.jdgarita.frnk.ui.nav.FrnkFullScreenRoute
import dev.jdgarita.frnk.ui.nav.FrnkNavDisplay
import dev.jdgarita.frnk.ui.nav.FrnkNavTab
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackHandler
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.nav.rememberFrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.scaffolds.LocalFrnkBottomBarInset
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
 * [onNestedNavigationModule]; the scaffold builds the per-tab [FrnkTabbedBackStacks] from those, loads
 * the module against them (`loadKoinModules`), and renders the core:
 *  - a single [FrnkNavDisplay] driven by the **active tab's** back stack,
 *  - the platform-adaptive [FrnkBottomFloatingBar] overlaid above it (so it persists across tab swaps),
 *  - tab switching + re-tap-the-active-tab-to-root,
 *  - the back-from-a-non-home-tab-root → home convention ([FrnkTabbedBackHandler]),
 *  - full-screen-route bar hiding ([hideBarFor], default `{ it is FrnkFullScreenRoute }`),
 *  - the bottom-inset bookkeeping (`LocalFrnkBottomBarInset`) so screens on
 *    `FrnkScreenScaffold` / `FrnkMviScreen` reserve the bar's footprint automatically.
 *
 * Each tab keeps its **own** back stack, so switching tabs never loses a tab's nested navigation and the
 * bar's selected highlight tracks the **active tab** (not the global stack depth) — pushing a detail onto
 * the active tab keeps that tab highlighted. The host's [onNestedNavigationModule] receives the
 * [FrnkTabbedBackStacks] handle and drives navigation through the active tab via
 * `tabbed.current.navigateTo(...)` / `tabbed.current.back()`.
 *
 * [FrnkTabbedNavScaffold] is the batteries-included fixed-three-tab app; reach for this lower-level
 * scaffold when you need a different tab shape and supply every destination yourself.
 *
 * @param tabs the bar tabs, each declared once ([FrnkNavTab]: key + root + icon + label + SF-Symbol).
 * @param onSavedStateConfiguration the saved-state config for the per-tab back stacks (e.g.
 *   `frnkNestedNavConfig(hostRoutes)`).
 * @param onNestedNavigationModule builds the Koin nav module registering the tabs' destinations,
 *   bound to the scaffold-owned [FrnkTabbedBackStacks].
 * @param initialTabKey the tab shown first; defaults to the first [tabs] entry.
 * @param homeTabKey the tab back returns to from any other tab's root; defaults to the first [tabs] entry.
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun FrnkNestedNavScaffold(
    tabs: List<FrnkNavTab>,
    onSavedStateConfiguration: () -> SavedStateConfiguration,
    onNestedNavigationModule: (tabbed: FrnkTabbedBackStacks) -> Module,
    modifier: Modifier = Modifier,
    initialTabKey: String = tabs.first().key,
    homeTabKey: String = tabs.first().key,
    hideBarFor: (NavKey) -> Boolean = { it is FrnkFullScreenRoute }
) {
    // The scaffold owns the per-tab back stacks + loads the host's nested module against them, so the
    // host supplies only the saved-state config + a module factory (and never touches the back stacks).
    val tabbed =
        rememberFrnkTabbedBackStacks(
            configuration = onSavedStateConfiguration(),
            navTabs = tabs,
            initialTabKey = initialTabKey,
            homeTabKey = homeTabKey
        )
    remember(tabbed) {
        loadKoinModules(onNestedNavigationModule(tabbed))
    }

    // Back from a non-home tab's root returns to the home tab; within-tab pops and the home-root exit are
    // handled by FrnkNavDisplay / the system.
    FrnkTabbedBackHandler(tabbed)

    val navBarItems =
        remember(tabs) {
            tabs.map { FrnkNavBarItem(key = it.key, icon = it.icon, iosSystemIcon = it.iosSystemIcon, label = it.label) }
        }
    // Derived from the active tab key (a snapshot read), so the highlight follows tab swaps.
    val selectedIndex = navBarItems.indexOfFirst { it.key == tabbed.currentTabKey }

    val onItemSelected: (Int) -> Unit = { index ->
        val key = navBarItems[index].key
        if (key == tabbed.currentTabKey) tabbed.resetCurrentToRoot() else tabbed.switchTo(key)
    }

    // Hide the bar on full-screen routes (paywall, onboarding, …); also guard against an unknown tab.
    val topRoute = tabbed.current.lastOrNull()
    val barVisible = selectedIndex >= 0 && (topRoute == null || !hideBarFor(topRoute))

    val reservedHeight = FrnkNavBarDefaults.reservedHeight
    val contentInset = if (barVisible) reservedHeight else 0.dp

    Box(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalFrnkBottomBarInset provides contentInset) {
            FrnkNavDisplay(
                backStack = tabbed.current,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (barVisible) {
            FrnkBottomFloatingBar(
                items = navBarItems,
                selectedIndex = selectedIndex,
                onItemSelected = onItemSelected,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
            )
        }
    }
}