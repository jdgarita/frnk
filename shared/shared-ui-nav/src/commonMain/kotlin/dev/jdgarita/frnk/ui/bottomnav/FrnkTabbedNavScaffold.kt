package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem
import dev.jdgarita.frnk.ui.nav.FrnkFullScreenRoute
import dev.jdgarita.frnk.ui.nav.FrnkNavDisplay
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackHandler
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.scaffolds.LocalFrnkBottomBarInset
import io.github.narendraanjana09.adaptivenavbar.Platform
import io.github.narendraanjana09.adaptivenavbar.getPlatform
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
 * **A/B engine (POC).** [engine] selects the bar implementation so a host can compare them at runtime:
 *  - [FrnkAdaptiveNavEngine.Calf] (**default**) — the native-`UITabBar`-on-iOS Calf bar; no primary-action
 *    button.
 *  - [FrnkAdaptiveNavEngine.AdaptiveNavBar] — the `adaptive-nav-bar` engine **with the built-in
 *    primary-action button**: pass [onPrimaryAction] to wire it (the host decides what tapping it does)
 *    and optionally a custom [primaryAction] to re-skin it per screen. On iOS the button renders inline
 *    beside the items; on Android this scaffold docks a Material3 FAB above the bar. The button shows only
 *    on this engine and only when [onPrimaryAction] is non-null. Tabs render through their
 *    `androidIcon`/`iosSystemIcon` here, through their `icon` `ImageVector` on Calf.
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
 * This is the **Material3 adaptive-bar** tabbed scaffold by design (both engines render through
 * `:shared-ui-nav`'s Material3 surface). A host that wants nav3 multiple-back-stack navigation **without**
 * Material3 wires the lower-level primitives directly — `rememberFrnkTabbedBackStacks` +
 * `FrnkNavDisplay(backStack = tabbed.current)` + `FrnkTabbedBackHandler` + a bar of its choosing.
 *
 * Contrast with [FrnkAdaptiveBottomNavScaffold], the simpler **index-based** scaffold (no per-tab back
 * stacks) — use that when each tab is a single screen; use this when tabs need their own back stacks.
 */
@OptIn(KoinExperimentalAPI::class)
@Composable
fun FrnkTabbedNavScaffold(
    tabbed: FrnkTabbedBackStacks,
    tabs: List<FrnkAdaptiveNavTab>,
    modifier: Modifier = Modifier,
    engine: FrnkAdaptiveNavEngine = FrnkAdaptiveNavEngine.Calf,
    primaryAction: FrnkNavPrimaryAction? = null,
    onPrimaryAction: (() -> Unit)? = null,
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
    val reservedHeight = FrnkAdaptiveBottomNavBarDefaults.reservedHeight
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

    Box(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalFrnkBottomBarInset provides contentInset) {
            FrnkNavDisplay(
                backStack = tabbed.current,
                modifier = Modifier.fillMaxSize(),
                entryProvider = entryProvider,
            )
        }

        if (barVisible) {
            when (engine) {
                FrnkAdaptiveNavEngine.Calf -> {
                    val calfItems = remember(tabs) { tabs.map { FrnkBottomNavItem(key = it.key, icon = it.icon, label = it.label) } }
                    FrnkAdaptiveBottomNavBar(
                        items = calfItems,
                        selectedIndex = selectedIndex,
                        onItemSelected = onItemSelected,
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    )
                }

                FrnkAdaptiveNavEngine.AdaptiveNavBar -> {
                    val navBarItems =
                        remember(tabs) {
                            tabs.map {
                                FrnkAdaptiveNavItem(
                                    key = it.key,
                                    label = it.label,
                                    androidIcon = it.androidIcon,
                                    iosSystemIcon = it.iosSystemIcon,
                                    selectedAndroidIcon = it.selectedAndroidIcon,
                                    selectedIosSystemIcon = it.selectedIosSystemIcon,
                                )
                            }
                        }
                    // Resolve the primary-action descriptor only when the action is actually wired, and
                    // only on this engine — so the default Calf path never pays for the Theme lookup +
                    // remember (the host-supplied [primaryAction] wins; otherwise fall back to the toolkit
                    // default).
                    val action = onPrimaryAction
                    val resolvedPrimaryAction =
                        if (action != null) primaryAction ?: rememberFrnkNavPrimaryAction() else null
                    FrnkAdaptiveNavBarBottomBar(
                        items = navBarItems,
                        selectedIndex = selectedIndex,
                        onItemSelected = onItemSelected,
                        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                        primaryAction = resolvedPrimaryAction,
                        onPrimaryAction = action,
                    )
                    // The library renders the primary-action FAB inline on iOS only; dock the Android FAB
                    // ourselves above the bar (guarded to Android via the library's getPlatform()).
                    if (action != null && resolvedPrimaryAction != null && getPlatform() == Platform.Android) {
                        FrnkAdaptiveNavBarPrimaryActionFab(
                            primaryAction = resolvedPrimaryAction,
                            onPrimaryAction = action,
                            modifier =
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 16.dp, bottom = reservedHeight + 16.dp),
                        )
                    }
                }
            }
        }
    }
}
