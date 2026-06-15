package dev.jdgarita.frnk.ui.nav

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

/**
 * A single bottom-nav tab for [FrnkTabbedNavScaffold]: its stable [key], the [root] destination its
 * back stack starts from, and the [icon] + [label] the adaptive bar shows for it.
 *
 * This is the richer sibling of [FrnkTab] (which carries only `key` + `root`): it folds the bar's
 * icon/label presentation into the same declaration, so a host describes each tab **once**
 * instead of declaring the back-stack tabs and the bar items as two parallel lists. The
 * `rememberFrnkTabbedBackStacks(navTabs = …)` overload seeds the back stacks from these, and
 * [FrnkTabbedNavScaffold] derives the bar items from the same list.
 */
@Immutable
data class FrnkNavTab(
    val key: String,
    val root: NavKey,
    val icon: ImageVector,
    val label: String,
)
