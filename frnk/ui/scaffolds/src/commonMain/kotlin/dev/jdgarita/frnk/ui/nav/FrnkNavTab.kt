package dev.jdgarita.frnk.ui.nav

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

/**
 * A single bottom-nav tab for `FrnkTabbedNavScaffold` / `FrnkNestedNavScaffold`: its stable [key], the
 * [root] destination its back stack starts from, and the two icon forms the adaptive bar needs —
 * [icon] (a Compose [ImageVector] for the Android floating toolbar) and [iosSystemIcon] (an SF-Symbol
 * string for the iOS native bar) — plus the [label].
 *
 * This is the richer sibling of [FrnkTab] (which carries only `key` + `root`): it folds the bar's
 * icon/label presentation into the same declaration, so a host describes each tab **once**
 * instead of declaring the back-stack tabs and the bar items as two parallel lists. The
 * `rememberFrnkTabbedBackStacks(navTabs = …)` overload seeds the back stacks from these, and the nested
 * scaffold derives the bar items from the same list.
 */
@Immutable
data class FrnkNavTab(
    val key: String,
    val root: NavKey,
    val icon: ImageVector,
    val label: String,
    val iosSystemIcon: String
)