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
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem
import dev.jdgarita.frnk.ui.nav.FrnkNavDisplay
import dev.jdgarita.frnk.ui.nav.FrnkNavTab
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackHandler
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.scaffolds.LocalFrnkBottomBarInset
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

/**
 * The toolkit's **Navigation3 multiple-back-stack** tabbed scaffold — the one composable a host calls to
 * get a standard tabbed app: a single [FrnkNavDisplay] driven by the active tab's back stack, the
 * platform-adaptive [FrnkAdaptiveBottomNavBar] overlaid above it (so it persists across tab swaps), tab
 * switching + re-tap-to-root, the "back from a non-home tab root returns to home" convention
 * ([FrnkTabbedBackHandler]), full-screen-route bar hiding, and the bottom-inset bookkeeping that lets
 * content scroll behind the bar — all absorbed here. A host that used to hand-wire ~120 lines of nav3
 * plumbing now writes one call.
 *
 * **The host still owns the back stacks** ([tabbed], from `rememberFrnkTabbedBackStacks`) and the same
 * [tabs] list ([FrnkNavTab] folds the back-stack root and the bar's icon/label into one declaration), so
 * the host can drive effect-based navigation (`tabbed.current.navigateTo(route)`) from its own
 * `EffectCollector`. This scaffold structures and renders; the host owns the state — matching the
 * toolkit's "host owns the back stack" philosophy.
 *
 * [hideBarFor] returns `true` for routes that should hide the bar (full-screen pushes like an onboarding
 * flow or a paywall); the bar's reserved height is provided through [LocalFrnkBottomBarInset] only while
 * it shows, so screens built on `FrnkScreenScaffold` / `FrnkMviScreen` reserve it automatically (no
 * per-screen `bottomInset` threading) and reserve nothing on a full-screen push.
 *
 * [entryProvider] defaults to Koin's [koinEntryProvider] (pair with the `navigation<Route> { … }` DSL);
 * pass a local `entryProvider { entry<Route> { … } }` to register destinations inline (e.g. when every
 * screen shares one host-scoped ViewModel, as the demo does).
 *
 * Contrast with [FrnkAdaptiveBottomNavScaffold], which is the simpler **index-based** scaffold (swaps
 * `tabContent` by selected index, no per-tab back stacks / no pushed detail screens) — use that when each
 * tab is a single screen; use this when tabs need their own navigation back stacks.
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun FrnkTabbedNavScaffold(
    tabbed: FrnkTabbedBackStacks,
    tabs: List<FrnkNavTab>,
    modifier: Modifier = Modifier,
    hideBarFor: (NavKey) -> Boolean = { false },
    entryProvider: (NavKey) -> NavEntry<NavKey> = koinEntryProvider(),
) {
    // Back from a non-home tab's root returns to the home tab (rather than exiting the app); within-tab
    // pops and the home-root exit are handled by FrnkNavDisplay / the system.
    FrnkTabbedBackHandler(tabbed)

    val top = tabbed.current.lastOrNull()
    val barHidden = top != null && hideBarFor(top)
    val selectedIndex = tabs.indexOfFirst { it.key == tabbed.currentTabKey }
    val barVisible = !barHidden && selectedIndex >= 0

    // Read the bar's reserved height unconditionally (it's a @Composable getter), then reserve it for
    // content only while the bar shows — exposed via LocalFrnkBottomBarInset so destinations on
    // FrnkScreenScaffold / FrnkMviScreen pick it up through their bottomInset default.
    val reservedHeight = FrnkAdaptiveBottomNavBarDefaults.reservedHeight
    val contentInset = if (barVisible) reservedHeight else 0.dp

    val barItems = remember(tabs) { tabs.map { FrnkBottomNavItem(key = it.key, icon = it.icon, label = it.label) } }

    Box(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalFrnkBottomBarInset provides contentInset) {
            FrnkNavDisplay(
                backStack = tabbed.current,
                modifier = Modifier.fillMaxSize(),
                entryProvider = entryProvider,
            )
        }

        if (barVisible) {
            FrnkAdaptiveBottomNavBar(
                items = barItems,
                selectedIndex = selectedIndex,
                onItemSelected = { index ->
                    val targetKey = tabs[index].key
                    if (targetKey == tabbed.currentTabKey) {
                        // Re-tapping the active tab returns it to its root, popping any pushed child.
                        tabbed.resetCurrentToRoot()
                    } else {
                        // Switch tabs, keeping each tab's own back stack (multiple back stacks).
                        tabbed.switchTo(targetKey)
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            )
        }
    }
}
