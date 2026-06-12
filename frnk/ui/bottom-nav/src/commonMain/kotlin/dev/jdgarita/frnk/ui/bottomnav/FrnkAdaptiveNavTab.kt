package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.DrawableResource

/**
 * A single bottom-nav tab for [FrnkTabbedNavScaffold]. It carries the resource-based icons the adaptive
 * bottom bar requires — [androidIcon] (`DrawableResource` for the Android Material3 bar) and
 * [iosSystemIcon] (SF-Symbol string for the iOS bar) — plus optional `selected*` variants that swap the
 * icon when the tab is active.
 *
 * It is the presentation sibling of `dev.jdgarita.frnk.ui.nav.FrnkNavTab`: same `key` + [root]
 * back-stack model, but with the bar's icon forms. Build the per-tab back stacks from the same list via
 * `rememberFrnkTabbedBackStacks(tabs = navTabs.map { FrnkTab(it.key, it.root) })`, and let
 * [FrnkTabbedNavScaffold] derive the bar items from it. [rememberFrnkAdaptiveNavTabs] builds the default
 * Home + middle + Settings list with the toolkit's bundled icons.
 */
@Immutable
data class FrnkAdaptiveNavTab(
    val key: String,
    val root: NavKey,
    val label: String,
    val androidIcon: DrawableResource,
    val iosSystemIcon: String,
    val selectedAndroidIcon: DrawableResource? = null,
    val selectedIosSystemIcon: String? = null,
)
