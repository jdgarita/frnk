package dev.jdgarita.frnk.ui.bottomnav.vendor

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource

/*
 * Vendored from io.github.narendraanjana09:adaptive-nav-bar (v1.0.1) by Narendra Anjana —
 * github.com/narendraanjana09/adaptive-navigation-bar. The upstream repository ships no LICENSE file;
 * the relevant iOS source is vendored here, with attribution, so the toolkit owns the native iOS bar
 * outright. The published library builds the whole `UITabBar` in its `UIKitView` factory and exposes no
 * Compose-animatable hook, which is exactly the control we need for the bar's slide/fade transitions.
 *
 * **iOS-only.** Android renders a Material3 `HorizontalFloatingToolbar` (see `FrnkBottomFloatingBar.android.kt`);
 * none of this is referenced from `commonMain` or `androidMain`. Trimmed vs upstream: the
 * `MaterialTheme`-defaulted `colors()` factory is replaced with one that takes explicit `FrnkTheme`
 * colors (the toolkit always themes the bar itself).
 */

/** A single tab in the native bar. `icon` (a `DrawableResource`) feeds only the older-iOS Compose
 *  fallback; the iOS-26 glass bar renders [systemIcon] (an SF-Symbol name). */
data class NavigationItem(
    val title: String,
    val icon: DrawableResource,
    val systemIcon: String,
    val selectedIcon: DrawableResource? = null,
    val selectedSystemIcon: String? = null,
    val showLabel: Boolean = true,
    val badge: String? = null,
    val showBadgeDot: Boolean = false,
    val enabled: Boolean = true,
    val contentDescription: String? = null,
)

/** The inline iOS primary-action button rendered beside the tabs (the glass FAB on iOS 26+). */
data class IosFabItem(
    val title: String = "",
    val showLabel: Boolean = false,
    val icon: DrawableResource,
    val systemIcon: String,
    val contentColor: Color,
    val containerColor: Color,
    val contentDescription: String? = null,
)

/** Resolved colors for the native bar (no `MaterialTheme` — the toolkit supplies `FrnkTheme` tokens). */
data class AdaptiveNavigationBarColors(
    val containerColor: Color,
    val indicatorColor: Color,
    val selectedIconColor: Color,
    val selectedTextColor: Color,
    val unselectedIconColor: Color,
    val unselectedTextColor: Color,
    val badgeContainerColor: Color,
    val badgeContentColor: Color,
)

object AdaptiveNavigationBarDefaults {
    /**
     * Builds [AdaptiveNavigationBarColors] from explicit colors. Badge colors are unused by the toolkit
     * (it shows no badges) and default off the supplied palette, so callers pass only the six visible
     * roles — matching the upstream call shape minus the `MaterialTheme` defaults.
     */
    fun colors(
        containerColor: Color,
        indicatorColor: Color,
        selectedIconColor: Color,
        selectedTextColor: Color,
        unselectedIconColor: Color,
        unselectedTextColor: Color,
        badgeContainerColor: Color = selectedIconColor,
        badgeContentColor: Color = containerColor,
    ) = AdaptiveNavigationBarColors(
        containerColor = containerColor,
        indicatorColor = indicatorColor,
        selectedIconColor = selectedIconColor,
        selectedTextColor = selectedTextColor,
        unselectedIconColor = unselectedIconColor,
        unselectedTextColor = unselectedTextColor,
        badgeContainerColor = badgeContainerColor,
        badgeContentColor = badgeContentColor,
    )
}
