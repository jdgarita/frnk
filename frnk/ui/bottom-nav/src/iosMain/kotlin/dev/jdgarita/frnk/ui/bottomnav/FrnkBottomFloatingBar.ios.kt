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
import dev.jdgarita.frnk.ui.bottomnav.vendor.IosFabItem
import dev.jdgarita.frnk.ui.bottomnav.vendor.NavigationItem
import dev.jdgarita.frnk.ui.theme.colorOnPrimary
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
 * The vendored bar's `NavigationItem.icon` / `IosFabItem.icon` are non-null `DrawableResource`s used only
 * by the older-iOS Compose fallback, but the common API speaks `ImageVector` (which the native `UITabBar`
 * cannot consume). We therefore feed it a single bundled [Res.drawable.frnk_nav_placeholder] for that slot;
 * on iOS 26+ it is never shown (the glass bar uses `systemIcon`).
 *
 * Themed from `FrnkTheme` tokens (the vendored defaults take no `MaterialTheme`): selected = `colorPrimary`,
 * unselected = `colorOnSurfaceVariant`, indicator = `colorPrimaryContainer`, surface = `colorSurface`.
 *
 * **Primary-action button.** When [primaryAction] and [onPrimaryAction] are both non-null, an [IosFabItem]
 * is handed to the bar, which renders the glass FAB **inline beside the nav items** and fires
 * [onPrimaryAction] via `onIosFabClick`.
 *
 * **Animated.** Unlike the old library wrapper, the bar is **not** wrapped in `key(...)`. The vendored
 * `AdaptiveNavigationBar` owns its `UIKitView` update block, so the FAB appearing/disappearing and the
 * tab bar sliding narrow↔full-width are **animated** (no recreate, no snap); theme color changes are
 * re-applied in place. The host can flip `primaryAction` per screen (e.g. Home only) and the bar
 * transitions smoothly.
 */
@Composable
actual fun FrnkBottomFloatingBar(
    items: List<FrnkNavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier,
    primaryAction: FrnkNavPrimaryAction?,
    onPrimaryAction: (() -> Unit)?,
) {
    val selectedColor = Theme[colors][colorPrimary]
    val unselectedColor = Theme[colors][colorOnSurfaceVariant]
    val indicatorColor = Theme[colors][colorPrimaryContainer]
    val containerColor = Theme[colors][colorSurface]
    val onPrimaryColor = Theme[colors][colorOnPrimary]

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

    val iosFab =
        if (primaryAction != null && onPrimaryAction != null) {
            IosFabItem(
                title = primaryAction.label,
                icon = Res.drawable.frnk_nav_placeholder,
                systemIcon = primaryAction.iosSystemIcon,
                contentColor = onPrimaryColor,
                containerColor = selectedColor,
                contentDescription = primaryAction.label,
            )
        } else {
            null
        }

    // The vendored AdaptiveNavigationBar takes no `modifier`, so wrap it to apply the caller's layout
    // (the scaffold aligns this at BottomCenter + fillMaxWidth). No `key(...)` — the bar animates FAB
    // presence / theme changes inside its own UIKitView update block.
    Box(modifier = modifier) {
        AdaptiveNavigationBar(
            items = navItems,
            iosFab = iosFab,
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
            onIosFabClick = { onPrimaryAction?.invoke() },
        )
    }
}
