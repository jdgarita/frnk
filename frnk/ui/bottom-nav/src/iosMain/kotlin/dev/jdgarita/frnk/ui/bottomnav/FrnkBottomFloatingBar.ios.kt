package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.bottomnav.generated.resources.Res
import dev.jdgarita.frnk.ui.bottomnav.generated.resources.frnk_nav_placeholder
import dev.jdgarita.frnk.ui.bottomnav.vendor.AdaptiveNavigationBar
import dev.jdgarita.frnk.ui.bottomnav.vendor.AdaptiveNavigationBarDefaults
import dev.jdgarita.frnk.ui.bottomnav.vendor.NavigationItem
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorPrimaryContainer
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors

/**
 * **iOS** [FrnkBottomFloatingBar] — the toolkit's **vendored** `AdaptiveNavigationBar` (a native glassy
 * `UITabBar` on iOS 26+ / Material3 Compose bar on older iOS), under `ui.bottomnav.vendor`. Each
 * [FrnkNavBarItem] renders from its [FrnkNavBarItem.iosSystemIcon] SF-Symbol on the native bar.
 *
 * The vendored bar's `NavigationItem.icon` is a non-null `DrawableResource` used only
 * by the older-iOS Compose fallback, but the common API speaks `ImageVector` (which the native `UITabBar`
 * cannot consume). We therefore feed it a single bundled [Res.drawable.frnk_nav_placeholder] for that slot;
 * on iOS 26+ it is never shown (the glass bar uses `systemIcon`).
 *
 * Themed from `FrnkTheme` tokens (the vendored defaults take no `MaterialTheme`): selected = `colorPrimary`,
 * unselected = `colorOnSurfaceVariant`, indicator = `colorPrimaryContainer`, surface = `colorSurface`.
 *
 * The toolkit's primary action is a permanent centered nav item (Mode B, injected by the scaffold), so the
 * bar has no FAB. The vendored `AdaptiveNavigationBar` still owns its `UIKitView` update block, so selection
 * and theme-color changes are re-applied in place (no recreate, no snap).
 */
@Composable
actual fun FrnkBottomFloatingBar(
    items: List<FrnkNavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier,
) {
    val selectedColor = Theme[colors][colorPrimary]
    val unselectedColor = Theme[colors][colorOnSurfaceVariant]
    val indicatorColor = Theme[colors][colorPrimaryContainer]
    val containerColor = Theme[colors][colorSurface]

    val navItems =
        remember(items) {
            items.map { item ->
                NavigationItem(
                    title = item.label,
                    // Placeholder for the vendored bar's non-null DrawableResource; the visible iOS 26+ icon
                    // is the SF-Symbol below. ImageVector cannot feed the native UITabBar, so item.icon is
                    // intentionally unused on iOS.
                    icon = Res.drawable.frnk_nav_placeholder,
                    systemIcon = item.iosSystemIcon,
                    // Icon-only — matches the Android pill (also label-less), and avoids the native
                    // UITabBar's stacked icon+label overlapping when the bar is in a custom narrow frame.
                    // `title`/`contentDescription` stay for accessibility.
                    showLabel = false,
                    contentDescription = item.label,
                )
            }
        }

    // The vendored AdaptiveNavigationBar takes no `modifier`, so wrap it to apply the caller's layout
    // (the scaffold aligns this at BottomCenter + fillMaxWidth). The bar re-applies theme changes in place
    // inside its own UIKitView update block.
    Box(modifier = modifier) {
        AdaptiveNavigationBar(
            items = navItems,
            selectedIndex = selectedIndex,
            colors =
                AdaptiveNavigationBarDefaults.colors(
                    containerColor = containerColor,
                    indicatorColor = indicatorColor,
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor,
                ),
            onItemSelected = onItemSelected,
        )
    }
}
