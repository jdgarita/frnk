package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.jdgarita.frnk.ui.atoms.frnkBottomSystemBarInset

/**
 * The toolkit's **platform-adaptive** bottom bar. The engine is `expect`/`actual` so each platform
 * renders with what it does best — and so Material3 never leaves Android:
 *  - **Android** — a Material3 *Expressive* `HorizontalFloatingToolbar` (a floating pill), drawing each
 *    item's [FrnkNavBarItem.icon] `ImageVector` directly. The toolkit's sole Material3 surface.
 *  - **iOS** — narendraanjana09's adaptive-nav-bar: a native glassy `UITabBar` (iOS 26+) / Material3
 *    Compose bar (older), driven by each item's [FrnkNavBarItem.iosSystemIcon] SF-Symbol.
 *
 * [onItemSelected] receives the tapped index. **Primary-action button:** when [primaryAction] and
 * [onPrimaryAction] are both non-null the bar shows a built-in add/primary button — the toolbar's docked
 * FAB on Android, the library's inline button on iOS. Both actuals theme from `FrnkTheme` tokens, not the
 * platform/Material defaults.
 *
 * This is the toolkit's sole bottom-bar engine. Most hosts reach it through [FrnkTabbedNavScaffold] (or
 * `FrnkAppShell` above it) rather than calling it directly. For a Material-free floating pill that lives
 * in the design-system tier, use `FrnkBottomNavBar` in `:ui-components` instead (a distinct component in a
 * different package).
 */
@Composable
expect fun FrnkBottomNavBar(
    items: List<FrnkNavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    primaryAction: FrnkNavPrimaryAction? = null,
    onPrimaryAction: (() -> Unit)? = null,
)

/** Shared layout metrics for the toolkit's adaptive bottom bar ([FrnkBottomNavBar]). */
object FrnkNavBarDefaults {
    /**
     * Body height of the bar. An **approximation** shared across both engines: the Android floating
     * pill is shorter than this and the iOS native `UITabBar` is ~49pt + safe-area — neither is an exact
     * match. Internal because hosts should reserve [reservedHeight] (body + the bottom system-bar inset)
     * when padding scrollable content; the bare body clips the last item on edge-to-edge devices. Read by
     * [FrnkTabbedNavScaffold] to inset content scrolling **behind** the overlaid bar.
     */
    internal val BarHeight: Dp = 80.dp

    /**
     * The bar's **full** reserved height = [BarHeight] + the bottom navigation-bar inset the bar sits
     * above. Hosts that **overlay** the bar over scrollable content (as [FrnkTabbedNavScaffold] does)
     * reserve this much as the content's bottom inset so the last item clears the bar. See [BarHeight] for
     * the per-platform approximation caveat.
     */
    val reservedHeight: Dp
        @Composable
        get() = BarHeight + frnkBottomSystemBarInset()
}
