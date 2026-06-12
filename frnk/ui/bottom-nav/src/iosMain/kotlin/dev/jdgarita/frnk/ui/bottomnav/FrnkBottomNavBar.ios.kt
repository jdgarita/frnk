package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.bottomnav.generated.resources.Res
import dev.jdgarita.frnk.ui.bottomnav.generated.resources.frnk_nav_placeholder
import dev.jdgarita.frnk.ui.theme.colorOnPrimary
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorPrimaryContainer
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors
import io.github.narendraanjana09.adaptivenavbar.AdaptiveNavigationBar
import io.github.narendraanjana09.adaptivenavbar.AdaptiveNavigationBarDefaults
import io.github.narendraanjana09.adaptivenavbar.IosFabItem
import io.github.narendraanjana09.adaptivenavbar.NavigationItem

/**
 * **iOS** [FrnkBottomNavBar] — narendraanjana09's `AdaptiveNavigationBar`: a native glassy `UITabBar`
 * (iOS 26+) / Material3 Compose bar (older iOS). Each [FrnkNavBarItem] renders from its
 * [FrnkNavBarItem.iosSystemIcon] SF-Symbol on the native bar.
 *
 * The library's `NavigationItem.icon` / `IosFabItem.icon` are non-null `DrawableResource`s used only by
 * the older-iOS Compose fallback, but the common API speaks `ImageVector` (which the native `UITabBar`
 * cannot consume). We therefore feed the library a single bundled [Res.drawable.frnk_nav_placeholder] for
 * that slot; on iOS 26+ it is never shown (the glass bar uses `systemIcon`).
 *
 * Themed from `FrnkTheme` tokens (the library colors default to `MaterialTheme`): selected = `colorPrimary`,
 * unselected = `colorOnSurfaceVariant`, indicator = `colorPrimaryContainer`, surface = `colorSurface`.
 *
 * **Primary-action button.** When [primaryAction] and [onPrimaryAction] are both non-null, an [IosFabItem]
 * is handed to the library, which renders it **inline beside the nav items** and fires [onPrimaryAction]
 * via `onIosFabClick`.
 */
@Composable
actual fun FrnkBottomNavBar(
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

    // The iOS 26+ bar captures its FAB tap handler once in the library's UIKitView factory and never
    // re-associates it (its update block only syncs the selected item), and the factory only re-runs when
    // we toggle `key(...)` below. rememberUpdatedState keeps the click delegating to the *latest*
    // onPrimaryAction, so a host that rebinds the callback (capturing changing state) isn't left firing a
    // stale closure while the FAB's presence is unchanged.
    val latestOnPrimaryAction by rememberUpdatedState(onPrimaryAction)

    val navItems =
        remember(items) {
            items.map { item ->
                NavigationItem(
                    title = item.label,
                    // Placeholder for the library's non-null DrawableResource; the visible iOS 26+ icon is
                    // the SF-Symbol below. ImageVector cannot feed the native UITabBar, so item.icon is
                    // intentionally unused on iOS.
                    icon = Res.drawable.frnk_nav_placeholder,
                    systemIcon = item.iosSystemIcon,
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

    // The library's AdaptiveNavigationBar takes no `modifier`, so wrap it to apply the caller's layout
    // (the scaffold aligns this at BottomCenter + fillMaxWidth).
    //
    // key(iosFab != null): on iOS 26+ the library builds the inline FAB once in its UIKitView *factory*
    // and its update block never adds/removes the FAB when `iosFab` toggles null↔non-null — so a host that
    // shows the primary action on some screens only (e.g. Home) would otherwise see the native FAB stick
    // around on every screen. Keying on the FAB's presence forces Compose to dispose + recreate the
    // UIKitView when it appears/disappears, so the factory re-runs with the current `iosFab`. (Switches
    // that don't change presence — e.g. between two FAB-less tabs — don't recreate; selectedIndex still
    // flows through the update block.)
    //
    // Known limitation: this recreation *snaps* the native bar into its new geometry (the iOS 26+ bar
    // shifts narrow/left ↔ full-width/centered as the FAB appears/disappears) with no transition. The lib
    // exposes no Compose-animatable hook — the frame is factory-set and the view is `placedAsOverlay`, so
    // Crossfade/alpha/animateContentSize can't touch it. A smooth slide would mean owning the iOS-26 bar
    // natively (iosMain + Core Animation); intentionally deferred. Spike record in the MobiAI brain
    // (`mobiai brain search "adaptive bottom nav"`).
    //
    // The color tokens are part of the key too: on iOS 26+ the library bakes the brand palette into the
    // native `UITabBarAppearance` in the factory (explicit UIColors, so a trait change doesn't re-resolve
    // them) and its update block never re-applies them. Without keying on the colors, a light↔dark toggle
    // while the FAB's presence is unchanged would leave the native bar's tints stale until the next
    // recreate. Color values are stable across recompositions (only the palette swap changes them), so this
    // recreates on theme toggle only — not every frame.
    Box(modifier = modifier) {
        key(iosFab != null, selectedColor, unselectedColor, indicatorColor, containerColor) {
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
                onIosFabClick = { latestOnPrimaryAction?.invoke() },
            )
        }
    }
}
