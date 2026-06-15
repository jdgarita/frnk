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
 * **Bar.** Renders [FrnkBottomFloatingBar] — a Material3 *Expressive* `HorizontalFloatingToolbar` (floating
 * pill) on Android and a native glassy `UITabBar` (iOS 26+) / Material3 bar (older) on iOS — **with a
 * built-in primary-action item**: pass [onPrimaryAction] to wire it (the host decides what tapping it
 * does) and optionally a custom [primaryAction] to re-skin it per screen. The scaffold injects it as a
 * permanent **centered bar item** (Mode B) on both platforms — no FAB. The item shows only when an action is
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
 * `FrnkNavDisplay(backStack = tabbed.current)` + `FrnkTabbedBackHandler` + a bar of its choosing.
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

    // The screen-registered handler wins over the host-level fallback; the action shows when either is
    // wired. Collected lifecycle-aware so a claim made/released while backgrounded settles on resume.
    val registeredAction =
        if (primaryActionRegistry != null) {
            val active by primaryActionRegistry.active.collectAsStateWithLifecycle()
            active
        } else {
            null
        }
    val effectivePrimaryAction = registeredAction ?: onPrimaryAction

    // The descriptor (icon/label) for the primary-action item — the host-supplied [primaryAction] wins,
    // otherwise the toolkit default ("+"). Resolved only when an action is actually wired.
    val defaultPrimary = rememberFrnkNavPrimaryAction()
    val primaryDescriptor = if (effectivePrimaryAction != null) primaryAction ?: defaultPrimary else null

    // **Mode B:** the primary action is a permanent **centered item** in the bar — not a docked/inline FAB.
    // When an action is wired (a screen claims it, or a host fallback), inject a "+" item at the centre of
    // the tabs; tapping it fires the action without changing the selected tab. The bar therefore stays
    // full-width/centred on every platform — no FAB, no narrowing slide, identical Android/iOS layout.
    val navBarItems =
        remember(tabs, primaryDescriptor) {
            val tabItems =
                tabs.map {
                    FrnkNavBarItem(key = it.key, icon = it.icon, iosSystemIcon = it.iosSystemIcon, label = it.label)
                }
            if (primaryDescriptor == null) {
                tabItems
            } else {
                tabItems.toMutableList().apply {
                    add(
                        (size + 1) / 2,
                        FrnkNavBarItem(
                            key = PRIMARY_ACTION_KEY,
                            icon = primaryDescriptor.icon,
                            iosSystemIcon = primaryDescriptor.iosSystemIcon,
                            label = primaryDescriptor.label,
                        ),
                    )
                }
            }
        }

    val selectedIndex = navBarItems.indexOfFirst { it.key == tabbed.currentTabKey }

    // Re-tap the active tab → pop to its root; tap another → switch (multiple back stacks). The injected
    // primary-action item fires the action and leaves the selected tab unchanged.
    val onItemSelected: (Int) -> Unit = { index ->
        val key = navBarItems[index].key
        when {
            key == PRIMARY_ACTION_KEY -> effectivePrimaryAction?.invoke()
            key == tabbed.currentTabKey -> tabbed.resetCurrentToRoot()
            else -> tabbed.switchTo(key)
        }
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
            LocalFrnkPrimaryActionRegistry provides primaryActionRegistry,
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

/** Key of the injected centered primary-action ("+") item — distinguishes it from real tabs in
 *  selection handling. Internal, never a real route key. */
private const val PRIMARY_ACTION_KEY = "__frnk_primary_action__"
