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
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackHandler
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.scaffolds.LocalFrnkBottomBarInset
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

/**
 * The toolkit's **Navigation3 multiple-back-stack** tabbed scaffold — the one composable a host calls to
 * get a standard tabbed app: a single [FrnkNavDisplay] driven by the active tab's back stack, the
 * platform-adaptive bottom bar overlaid above it (so it persists across tab swaps), tab switching +
 * re-tap-to-root, the "back from a non-home tab root returns to home" convention
 * ([FrnkTabbedBackHandler]), full-screen-route bar hiding, and the bottom-inset bookkeeping that lets
 * content scroll behind the bar — all absorbed here. A host that used to hand-wire ~120 lines of nav3
 * plumbing now writes one call.
 *
 * **The host still owns the back stacks** ([tabbed], from `rememberFrnkTabbedBackStacks`) and the same
 * [tabs] list ([FrnkAdaptiveNavTab] folds the back-stack root and **both** bar-engine icon forms into one
 * declaration), so the host can drive effect-based navigation (`tabbed.current.navigateTo(route)`) from
 * its own `EffectCollector`. This scaffold structures and renders; the host owns the state. Pass a
 * **remembered** [tabs] list (a plain `List` is an unstable Compose parameter); build the back stacks
 * from the same list via `rememberFrnkTabbedBackStacks(tabs = navTabs.map { FrnkTab(it.key, it.root) })`,
 * or use [rememberFrnkBottomNavState] which returns the remembered fixed `Home · feature · Settings`
 * [FrnkBottomNavState] (pass its `tabs` here).
 *
 * **Bar.** Renders [FrnkBottomFloatingBar] — a Material3 *Expressive* `HorizontalFloatingToolbar` (floating
 * pill) on Android and a native glassy `UITabBar` (iOS 26+) / Material3 bar (older) on iOS — showing one
 * item per tab (no FAB, no injected items): the bar always reflects exactly the [tabs] handed in. Tabs
 * render through their `icon` (`ImageVector`, Android) / `iosSystemIcon` (SF-Symbol, iOS). Tapping a tab
 * switches to its back stack; re-tapping the active tab pops it to its root.
 *
 * [hideBarFor] returns `true` for routes that should hide the bar (full-screen pushes like an onboarding
 * flow or a paywall). It **defaults to `{ it is FrnkFullScreenRoute }`**, so a route declares the intent
 * on itself rather than the host maintaining a separate predicate. The bar's reserved height is provided
 * through [LocalFrnkBottomBarInset] only while it shows, so screens built on `FrnkScreenScaffold` /
 * `FrnkMviScreen` reserve it automatically (no per-screen `bottomInset` threading).
 *
 * [entryProvider] defaults to Koin's [koinEntryProvider] (pair with the `navigation<Route> { … }` DSL);
 * pass a local `entryProvider { entry<Route> { … } }` to register destinations inline.
 *
 * This is the **Material3 adaptive-bar** tabbed scaffold by design (the bar renders through this module's
 * Material3 surface). A host that wants nav3 multiple-back-stack navigation **without** Material3 wires the
 * lower-level primitives directly — `rememberFrnkTabbedBackStacks` +
 * `FrnkNavDisplay(backStack = tabbed.current)` + `FrnkTabbedBackHandler` + a bar of its choosing.
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun FrnkTabbedNavScaffold(
    tabbed: FrnkTabbedBackStacks,
    tabs: List<FrnkAdaptiveNavTab>,
    modifier: Modifier = Modifier,
    hideBarFor: (NavKey) -> Boolean = { it is FrnkFullScreenRoute },
    entryProvider: (NavKey) -> NavEntry<NavKey> = koinEntryProvider(),
) {
    // Back from a non-home tab's root returns to the home tab (rather than exiting the app); within-tab
    // pops and the home-root exit are handled by FrnkNavDisplay / the system.
    FrnkTabbedBackHandler(tabbed)

    // One bar item per tab — the bar always shows exactly the tabs handed in (no FAB, no injected item).
    val navBarItems =
        remember(tabs) {
            tabs.map {
                FrnkNavBarItem(key = it.key, icon = it.icon, iosSystemIcon = it.iosSystemIcon, label = it.label)
            }
        }

    val selectedIndex = navBarItems.indexOfFirst { it.key == tabbed.currentTabKey }

    // Re-tap the active tab → pop to its root; tap another → switch (multiple back stacks).
    val onItemSelected: (Int) -> Unit = { index ->
        val key = navBarItems[index].key
        if (key == tabbed.currentTabKey) tabbed.resetCurrentToRoot() else tabbed.switchTo(key)
    }

    // Hide the bar on full-screen routes (paywall, onboarding, …); also guard against an unknown tab.
    val top = tabbed.current.lastOrNull()
    val barVisible = (top == null || !hideBarFor(top)) && selectedIndex >= 0

    // Reserve the bar's footprint as the content's bottom inset (read unconditionally — it's a @Composable
    // getter) so screens on FrnkScreenScaffold / FrnkMviScreen pad their scrollable content above the bar
    // and the last item isn't hidden behind it. Provided as 0 while the bar is hidden.
    val reservedHeight = FrnkNavBarDefaults.reservedHeight
    val contentInset = if (barVisible) reservedHeight else 0.dp

    Box(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalFrnkBottomBarInset provides contentInset,
        ) {
            FrnkNavDisplay(
                backStack = tabbed.current,
                modifier = Modifier.fillMaxSize(),
                entryProvider = entryProvider,
            )
        }

        if (barVisible) {
            FrnkBottomFloatingBar(
                items = navBarItems,
                selectedIndex = selectedIndex,
                onItemSelected = onItemSelected,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            )
        }
    }
}
