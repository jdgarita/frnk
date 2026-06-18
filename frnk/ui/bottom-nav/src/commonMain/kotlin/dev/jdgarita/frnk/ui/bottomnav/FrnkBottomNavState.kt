package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Immutable

/**
 * The view state backing the toolkit's adaptive bottom bar ([FrnkBottomFloatingBar], via
 * [FrnkTabbedNavScaffold]). It models the toolkit's fixed **three-tab** shape — `Home · feature ·
 * Settings`, in that order — and is the reason the bar always shows exactly three items on every
 * screen.
 *
 * **Only the [feature] (center) tab is host-configurable.** [home] and [settings] are built internally
 * from theme tokens by [rememberFrnkBottomNavState] and cannot be supplied or reordered — hence the
 * `internal` constructor: a host shapes the bar solely by passing a [FrnkFeatureItem]. The product
 * contract "every app has at least Home and Settings" is therefore structural, not a runtime check.
 *
 * @property home the fixed leading Home tab (toolkit-owned).
 * @property feature the host's configurable center tab.
 * @property settings the fixed trailing Settings tab (toolkit-owned).
 * @property tabs the three tabs in render order — what [FrnkTabbedNavScaffold] maps to bar items and
 *   the host maps to per-tab back stacks (`tabs.map { FrnkTab(it.key, it.root) }`).
 */
@Immutable
class FrnkBottomNavState internal constructor(
    val home: FrnkBottomNavTab.Home,
    val feature: FrnkBottomNavTab.Feature,
    val settings: FrnkBottomNavTab.Settings
) {
    val tabs: List<FrnkBottomNavTab> = listOf(home, feature, settings)
}