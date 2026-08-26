package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource

/**
 * A single item rendered by [FrnkBottomFloatingBar]. Icons are split by what each platform's engine can
 * actually consume:
 *  - [icon] — a Compose [ImageVector], rendered directly by the Android Material3 `HorizontalFloatingToolbar`.
 *  - [iosSystemIcon] — an SF-Symbol name, rendered by the native glassy `UITabBar` (iOS 26+). The native
 *    bar takes a UIKit symbol, not a Compose vector, so the iOS identifier stays explicit here.
 *
 * [label] doubles as the accessibility content description; it is a [FrnkStringSource], resolved by
 * each platform bar at render time so token-backed labels localize with the theme. Selection is
 * conveyed by the bar's `selectedIndex` + tint — there are no separate selected-icon variants (MVP).
 */
@Immutable
data class FrnkNavBarItem(
    val key: String,
    val icon: FrnkIconSource,
    val iosSystemIcon: String,
    val label: FrnkStringSource
)