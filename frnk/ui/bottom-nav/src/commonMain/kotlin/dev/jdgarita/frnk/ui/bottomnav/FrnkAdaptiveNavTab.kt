package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey

/**
 * A single bottom-nav tab for [FrnkTabbedNavScaffold]. It carries the two icon forms the adaptive bottom
 * bar needs — [icon] (a Compose [ImageVector] for the Android floating toolbar) and [iosSystemIcon] (an
 * SF-Symbol string for the iOS native bar) — alongside the tab's back-stack [root].
 *
 * It is the presentation sibling of `dev.jdgarita.frnk.ui.nav.FrnkNavTab`: same `key` + [root]
 * back-stack model, but with the bar's icon forms. Build the per-tab back stacks from the same list via
 * `rememberFrnkTabbedBackStacks(tabs = navTabs.map { FrnkTab(it.key, it.root) })`, and let
 * [FrnkTabbedNavScaffold] derive the bar items from it. [rememberFrnkBottomNavState] builds the fixed
 * Home + feature + Settings tabs (as a [FrnkBottomNavState]) with the toolkit's theme icons.
 */
@Immutable
data class FrnkAdaptiveNavTab(
    val key: String,
    val root: NavKey,
    val label: String,
    val icon: ImageVector,
    val iosSystemIcon: String,
)
