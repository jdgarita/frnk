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
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.ui.nav.FrnkFullScreenRoute
import dev.jdgarita.frnk.ui.nav.FrnkNavDisplay
import dev.jdgarita.frnk.ui.nav.FrnkNavTab
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackHandler
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.scaffolds.LocalFrnkBottomBarInset
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

/**
 * A **flexible multiple-back-stack tabbed scaffold** — the toolkit's bottom-bar navigation for a host
 * that wants its **own** set of tabs (any count, any roots, all destinations host-supplied) rather than
 * the fixed `Home · feature · Settings` shape + built-in Home/Settings destinations of
 * [FrnkTabbedNavScaffold].
 *
 * The host owns the [tabbed] back stacks (built once with
 * `rememberFrnkTabbedBackStacks(config, navTabs = tabs)`) and the [entryProvider] (its destinations);
 * this scaffold owns the render core:
 *  - a single [FrnkNavDisplay] driven by the **active tab's** back stack,
 *  - the platform-adaptive [FrnkBottomFloatingBar] overlaid above it (so it persists across tab swaps),
 *  - tab switching + re-tap-the-active-tab-to-root,
 *  - the back-from-a-non-home-tab-root → home convention ([FrnkTabbedBackHandler]),
 *  - full-screen-route bar hiding ([hideBarFor], default `{ it is FrnkFullScreenRoute }`),
 *  - the bottom-inset bookkeeping (`LocalFrnkBottomBarInset`) so screens on
 *    `FrnkScreenScaffold` / `FrnkMviScreen` reserve the bar's footprint automatically.
 *
 * Each tab keeps its **own** back stack, so switching tabs never loses a tab's nested navigation and the
 * bar's selected highlight tracks the **active tab** (not the global stack depth). Pushing a detail onto
 * the active tab therefore keeps that tab highlighted.
 *
 * [FrnkTabbedNavScaffold] is the batteries-included superset built on this same render core; reach for
 * this lower-level scaffold when you need a different tab shape or supply every destination yourself.
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun FrnkNestedNavScaffold(
    tabbed: FrnkTabbedBackStacks,
    tabs: List<FrnkNavTab>,
    modifier: Modifier = Modifier,
    hideBarFor: (NavKey) -> Boolean = { it is FrnkFullScreenRoute },
    entryProvider: (NavKey) -> NavEntry<NavKey> = koinEntryProvider()
) {
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
                modifier = Modifier.fillMaxSize(),
                entryProvider = entryProvider
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