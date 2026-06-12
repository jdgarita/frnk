package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.ui.nav.FrnkFullScreenRoute
import dev.jdgarita.frnk.ui.nav.FrnkNavDisplay
import dev.jdgarita.frnk.ui.nav.FrnkPrimaryActionRegistry
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackHandler
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.nav.LocalFrnkPrimaryActionRegistry
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
 * or use [rememberFrnkAdaptiveNavTabs] which returns a remembered Home + middle + Settings list.
 *
 * **Bar.** Renders [FrnkBottomNavBar] — a Material3 *Expressive* `HorizontalFloatingToolbar` (floating
 * pill) on Android and a native glassy `UITabBar` (iOS 26+) / Material3 bar (older) on iOS — **with a
 * built-in primary-action button**: pass [onPrimaryAction] to wire it (the host decides what tapping it
 * does) and optionally a custom [primaryAction] to re-skin it per screen. The bar's platform actual renders
 * the button — a docked FAB on Android, an inline button on iOS. The button shows only when an action is
 * wired. Tabs render through their `icon` (`ImageVector`, Android) / `iosSystemIcon` (SF-Symbol, iOS).
 *
 * **Primary-action routing.** Two complementary ways to wire the button:
 *  - [primaryActionRegistry] — pass a remembered [FrnkPrimaryActionRegistry] and the **currently active
 *    screen** claims the button for its lifetime via `FrnkPrimaryActionHandler { … }` (typically sending
 *    an MVI intent to its own ViewModel). The scaffold provides the registry through
 *    [LocalFrnkPrimaryActionRegistry] to every destination; the button shows only while some screen
 *    holds a claim. This replaces hand-rolled `tabbed.currentTabKey == …` conditionals at the host root.
 *  - [onPrimaryAction] — a host-level fallback used while **no** screen has registered a handler
 *    (or when no registry is passed at all — the pre-registry behavior, unchanged).
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
 * `FrnkNavDisplay(backStack = tabbed.current)` + `FrnkTabbedBackHandler` + a bar of its choosing (e.g. the
 * Material-free `FrnkBottomNavBar` pill in `:ui-components`).
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun FrnkTabbedNavScaffold(
    tabbed: FrnkTabbedBackStacks,
    tabs: List<FrnkAdaptiveNavTab>,
    modifier: Modifier = Modifier,
    primaryAction: FrnkNavPrimaryAction? = null,
    onPrimaryAction: (() -> Unit)? = null,
    primaryActionRegistry: FrnkPrimaryActionRegistry? = null,
    hideBarFor: (NavKey) -> Boolean = { it is FrnkFullScreenRoute },
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
    val reservedHeight = FrnkNavBarDefaults.reservedHeight
    val contentInset = if (barVisible) reservedHeight else 0.dp

    // Shared tab-bar behaviour for both engines: re-tap the active tab → pop to its root; tap another →
    // switch, keeping each tab's own back stack (multiple back stacks).
    val onItemSelected: (Int) -> Unit = { index ->
        val targetKey = tabs[index].key
        if (targetKey == tabbed.currentTabKey) {
            tabbed.resetCurrentToRoot()
        } else {
            tabbed.switchTo(targetKey)
        }
    }

    // The screen-registered handler wins over the host-level fallback; the button hides when neither
    // is wired. Collected lifecycle-aware so a claim made/released while backgrounded settles on resume.
    val registeredAction =
        if (primaryActionRegistry != null) {
            val active by primaryActionRegistry.active.collectAsStateWithLifecycle()
            active
        } else {
            null
        }
    val effectivePrimaryAction = registeredAction ?: onPrimaryAction

    Box(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalFrnkBottomBarInset provides contentInset,
            LocalFrnkPrimaryActionRegistry provides primaryActionRegistry,
        ) {
            FrnkNavDisplay(
                backStack = tabbed.current,
                modifier = Modifier.fillMaxSize(),
                entryProvider = entryProvider,
            )
        }

        if (barVisible) {
            val navBarItems =
                remember(tabs) {
                    tabs.map {
                        FrnkNavBarItem(
                            key = it.key,
                            icon = it.icon,
                            iosSystemIcon = it.iosSystemIcon,
                            label = it.label,
                        )
                    }
                }
            // Resolve the primary-action descriptor only when the action is actually wired (the
            // host-supplied [primaryAction] wins; otherwise fall back to the toolkit default). The bar's
            // platform actual renders it — a docked FAB on Android, an inline button on iOS — so this
            // scaffold no longer docks a platform-specific FAB itself.
            val action = effectivePrimaryAction
            val resolvedPrimaryAction =
                if (action != null) primaryAction ?: rememberFrnkNavPrimaryAction() else null
            FrnkBottomNavBar(
                items = navBarItems,
                selectedIndex = selectedIndex,
                onItemSelected = onItemSelected,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                primaryAction = resolvedPrimaryAction,
                onPrimaryAction = action,
            )
        }
    }
}
