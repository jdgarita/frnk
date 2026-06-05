package dev.jdgarita.frnk.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlin.jvm.JvmName

/** A bottom-nav tab: its stable [key] and the [root] destination its back stack starts from. */
data class FrnkTab(
    val key: String,
    val root: NavKey,
)

/**
 * Holds one [NavBackStack] per bottom-nav tab so each tab keeps its **own** back stack across tab swaps —
 * the "multiple back stacks" pattern. Navigation3 has no built-in `saveState`/`restoreState` (the nav2
 * tab recipe), so the toolkit models it explicitly: switching tabs just swaps which stack [current]
 * returns; the leaving tab's stack is retained untouched. Feed [current] to a single `FrnkNavDisplay`.
 *
 * Created via [rememberFrnkTabbedBackStacks]. The host derives the pill highlight from [currentTabKey] and
 * bar visibility from the [current] stack's top (e.g. hide the bar when a full-screen route was pushed).
 */
@Stable
class FrnkTabbedBackStacks internal constructor(
    private val stacks: Map<String, NavBackStack<NavKey>>,
    private val roots: Map<String, NavKey>,
    private val currentTabKeyState: MutableState<String>,
    /**
     * The "home" tab — the one back returns to from any other tab's root (see [FrnkTabbedBackHandler]).
     * Defaults to the first tab.
     */
    val homeTabKey: String,
) {
    /** The currently selected tab's key. Drives the bottom bar's selected index. Survives process death. */
    val currentTabKey: String get() = currentTabKeyState.value

    /** The active tab's back stack — pass this to `FrnkNavDisplay(backStack = …)`. */
    val current: NavBackStack<NavKey> get() = stacks.getValue(currentTabKey)

    /** The active tab's root destination. */
    val currentRoot: NavKey get() = roots.getValue(currentTabKey)

    /** True when the active tab sits at its root (nothing pushed on top). */
    val isCurrentAtRoot: Boolean get() = current.size <= 1

    /** Select [tabKey], keeping that tab's existing back stack. No-op for an unknown key. */
    fun switchTo(tabKey: String) {
        if (tabKey in stacks) currentTabKeyState.value = tabKey
    }

    /** True when a non-home tab sits at its root — the case where back should return to [homeTabKey]. */
    val canReturnToHome: Boolean get() = currentTabKey != homeTabKey && isCurrentAtRoot

    /** Switch to the [homeTabKey] tab (the back-from-a-non-home-root gesture). */
    fun switchToHome() = switchTo(homeTabKey)

    /** Pop the active tab back to its root (the re-tap-the-active-tab gesture). Keeps the root entry's state. */
    fun resetCurrentToRoot() {
        val stack = current
        while (stack.size > 1) stack.removeLastOrNull()
    }
}

/**
 * Remember a [FrnkTabbedBackStacks] for [tabs], each tab seeded with its own [NavBackStack] (built from the
 * shared [configuration] so every tab's stack persists/restores across configuration change and process
 * death). [initialTabKey] defaults to the first tab; [homeTabKey] (the tab back returns to from any other
 * tab's root — see [FrnkTabbedBackHandler]) also defaults to the first tab.
 */
@Composable
fun rememberFrnkTabbedBackStacks(
    configuration: SavedStateConfiguration,
    tabs: List<FrnkTab>,
    initialTabKey: String = tabs.first().key,
    homeTabKey: String = tabs.first().key,
): FrnkTabbedBackStacks {
    require(tabs.isNotEmpty()) { "FrnkTabbedBackStacks needs at least one tab" }
    // key(tab.key) gives each per-tab rememberNavBackStack a stable, distinct saveable slot.
    val stacks =
        tabs.associate { tab ->
            tab.key to key(tab.key) { rememberFrnkNavBackStack(configuration, tab.root) }
        }
    val roots = remember(tabs) { tabs.associate { it.key to it.root } }
    val currentTabKeyState = rememberSaveable { mutableStateOf(initialTabKey) }
    return remember(stacks, roots, homeTabKey) {
        FrnkTabbedBackStacks(
            stacks = stacks,
            roots = roots,
            currentTabKeyState = currentTabKeyState,
            homeTabKey = homeTabKey,
        )
    }
}

/**
 * [rememberFrnkTabbedBackStacks] overload that seeds the per-tab back stacks from a [FrnkNavTab] list —
 * the same list [FrnkTabbedNavScaffold] derives the bar items from, so a host describes each tab once.
 * Maps each [FrnkNavTab] to its `key`/`root` and delegates to the [FrnkTab]-based factory.
 *
 * `@JvmName`'d because `List<FrnkNavTab>` and `List<FrnkTab>` erase to the same JVM signature; the Kotlin
 * call name stays `rememberFrnkTabbedBackStacks` (overload resolution picks by element type). No-op on iOS.
 */
@JvmName("rememberFrnkTabbedBackStacksFromNavTabs")
@Composable
fun rememberFrnkTabbedBackStacks(
    configuration: SavedStateConfiguration,
    navTabs: List<FrnkNavTab>,
    initialTabKey: String = navTabs.first().key,
    homeTabKey: String = navTabs.first().key,
): FrnkTabbedBackStacks {
    val tabs = remember(navTabs) { navTabs.map { FrnkTab(key = it.key, root = it.root) } }
    return rememberFrnkTabbedBackStacks(
        configuration = configuration,
        tabs = tabs,
        initialTabKey = initialTabKey,
        homeTabKey = homeTabKey,
    )
}

/**
 * Wires the platform back signal (Android system/predictive back, iOS swipe-back) to the bottom-nav
 * "return to home" convention: when a non-home tab sits at its root, back switches to
 * [FrnkTabbedBackStacks.homeTabKey] instead of falling through to the system and **leaving the app** — the
 * behavior users expect from a tabbed app. On the home tab's root this handler is disabled, so back exits
 * the app as usual.
 *
 * This complements `FrnkNavDisplay`'s own back handler, which pops *within* the current tab whenever its
 * stack has more than one entry. The two are disjoint by construction (this one is enabled only at a tab
 * root, where there's nothing to pop), so they never both fire. Place this once at the host scope, paired
 * with a `FrnkNavDisplay(backStack = tabbed.current)` driven by the same [tabbed].
 *
 * [enabled] gates the whole behavior off (e.g. while a search field is open and should swallow back first).
 */
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("DEPRECATION")
@Composable
fun FrnkTabbedBackHandler(
    tabbed: FrnkTabbedBackStacks,
    enabled: Boolean = true,
) {
    BackHandler(enabled = enabled && tabbed.canReturnToHome) {
        tabbed.switchToHome()
    }
}
