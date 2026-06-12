package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.frnkBottomSystemBarInset
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
import org.jetbrains.compose.resources.painterResource

/** Shared layout metrics for the toolkit's adaptive bottom bar ([FrnkAdaptiveNavBarBottomBar]). */
object FrnkAdaptiveBottomNavBarDefaults {
    /**
     * Body height of the bar — the Android Material3 `NavigationBar`'s fixed 80.dp. Internal because
     * hosts should reserve [reservedHeight] (which adds the bottom system-bar inset) when padding
     * scrollable content; the bare body clips the last item on edge-to-edge devices.
     *
     * **iOS is an approximation.** The native `UITabBar` is ~49pt tall (plus the bottom safe-area
     * inset) — close to but not exactly 80.dp, and Compose's `WindowInsets.navigationBars` is the
     * Compose-reported inset, not UIKit's `safeAreaInsets.bottom`. So on iOS [reservedHeight] is a
     * generous estimate of the native bar's footprint, not a pixel-exact match. It is read by
     * [FrnkTabbedNavScaffold] to inset content scrolling **behind** the overlaid bar.
     */
    internal val BarHeight: Dp = 80.dp

    /**
     * The bar's **full** reserved height = [BarHeight] + the bottom navigation-bar inset the bar sits
     * above (the Material3 `NavigationBar` consumes `WindowInsets.navigationBars`, so its real height
     * grows by that inset on devices with a non-zero bottom inset). Hosts that **overlay** the bar over
     * scrollable content (as [FrnkTabbedNavScaffold] does) reserve this much as the content's bottom
     * inset so the last item clears the bar — reserving only [BarHeight] clips it. See [BarHeight] for
     * the iOS approximation caveat.
     */
    val reservedHeight: Dp
        @Composable
        get() = BarHeight + frnkBottomSystemBarInset()
}

/**
 * The toolkit's **platform-adaptive** bottom bar, wrapping
 * [narendraanjana09/adaptive-nav-bar](https://github.com/narendraanjana09/adaptive-navigation-bar)'s
 * `AdaptiveNavigationBar` — a Material3 `NavigationBar` on Android and a native glassy `UITabBar`
 * (iOS 26+) / Material3 bar (older) on iOS. Generic over [FrnkAdaptiveNavItem] (resource icons, since
 * the library takes `DrawableResource` + SF-Symbol strings, not `ImageVector`); [onItemSelected]
 * receives the tapped index.
 *
 * **Primary-action button.** When [primaryAction] and [onPrimaryAction] are both non-null:
 *  - **iOS** — an [IosFabItem] is handed to the library, which renders it **inline beside the nav items**
 *    and fires [onPrimaryAction] via `onIosFabClick`.
 *  - **Android** — the library does **not** render a FAB; the caller renders one with
 *    [FrnkAdaptiveNavBarPrimaryActionFab] (the [FrnkTabbedNavScaffold] docks it above the bar, guarded to
 *    Android).
 *
 * Themed from `FrnkTheme` tokens, not the platform/Material defaults — the library colors default to
 * `MaterialTheme`, so we pass the brand palette explicitly (selected = `colorPrimary`, unselected =
 * `colorOnSurfaceVariant`, indicator = `colorPrimaryContainer`, surface = `colorSurface`).
 *
 * This is the toolkit's sole bottom-bar engine. Most hosts reach it through [FrnkTabbedNavScaffold]
 * (or `FrnkAppShell` above it) rather than calling it directly. For the Material-free floating pill,
 * use `FrnkBottomNavBar` in `:ui-components` instead.
 */
@Composable
fun FrnkAdaptiveNavBarBottomBar(
    items: List<FrnkAdaptiveNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    primaryAction: FrnkNavPrimaryAction? = null,
    onPrimaryAction: (() -> Unit)? = null,
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
                    icon = item.androidIcon,
                    systemIcon = item.iosSystemIcon,
                    selectedIcon = item.selectedAndroidIcon,
                    selectedSystemIcon = item.selectedIosSystemIcon,
                    contentDescription = item.label,
                )
            }
        }

    // The library renders the FAB inline only on iOS; on Android it's the caller's job (see the Fab below).
    val iosFab =
        if (primaryAction != null && onPrimaryAction != null) {
            IosFabItem(
                title = primaryAction.label,
                icon = primaryAction.androidIcon,
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

/**
 * The Android FAB for the bar's primary-action button — a Material3
 * `FloatingActionButton` tinted from `FrnkTheme` tokens. The library doesn't render the Android FAB
 * itself, so [FrnkTabbedNavScaffold] docks this above the bar (guarded to Android via the library's
 * `getPlatform()`); on iOS the FAB comes from the [IosFabItem] inside [FrnkAdaptiveNavBarBottomBar].
 */
@Composable
fun FrnkAdaptiveNavBarPrimaryActionFab(
    primaryAction: FrnkNavPrimaryAction,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onPrimaryAction,
        modifier = modifier,
        containerColor = Theme[colors][colorPrimary],
        contentColor = Theme[colors][colorOnPrimary],
    ) {
        Icon(
            painter = painterResource(primaryAction.androidIcon),
            contentDescription = primaryAction.label,
        )
    }
}
