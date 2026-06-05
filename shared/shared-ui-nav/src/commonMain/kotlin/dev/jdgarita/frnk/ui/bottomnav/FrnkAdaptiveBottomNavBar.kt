package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.AdaptiveNavigationBar
import com.mohamedrejeb.calf.ui.navigation.UIKitTabBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem
import dev.jdgarita.frnk.ui.atoms.frnkBottomSystemBarInset
import dev.jdgarita.frnk.ui.theme.colorOnSurface
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorPrimaryContainer
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors

/** Shared layout metrics for [FrnkAdaptiveBottomNavBar]. */
object FrnkAdaptiveBottomNavBarDefaults {
    /**
     * Body height of the bar — the Android Material3 `NavigationBar`'s fixed 80.dp. Internal because
     * hosts should reserve [reservedHeight] (which adds the bottom system-bar inset) when padding
     * scrollable content; the bare body clips the last item on edge-to-edge devices.
     *
     * **iOS is an approximation.** The native `UITabBar` is ~49pt tall (plus the bottom safe-area
     * inset) — close to but not exactly 80.dp, and Compose's `WindowInsets.navigationBars` is the
     * Compose-reported inset, not UIKit's `safeAreaInsets.bottom`. So on iOS [reservedHeight] is a
     * generous estimate of the native bar's footprint, not a pixel-exact match. It's only used for the
     * **overlay** path (content scrolling behind the bar); the default [FrnkAdaptiveBottomNavScaffold]
     * lays content *above* the bar and doesn't read this, so the approximation never bites there.
     */
    internal val BarHeight: Dp = 80.dp

    /**
     * The bar's **full** reserved height = [BarHeight] + the bottom navigation-bar inset the bar sits
     * above (the Material3 `NavigationBar` consumes `WindowInsets.navigationBars`, so its real height
     * grows by that inset on devices with a non-zero bottom inset). Hosts that **overlay** the bar over
     * scrollable content (e.g. a bar pinned at `Alignment.BottomCenter` over a `NavHost`) must reserve
     * this much as the content's bottom inset so the last item clears the bar — reserving only
     * [BarHeight] clips it. (Hosts using [FrnkAdaptiveBottomNavScaffold], which lays content *above* the
     * bar rather than behind it, don't need this — the scaffold already accounts for the bar's space.)
     * See [BarHeight] for the iOS approximation caveat.
     */
    val reservedHeight: Dp
        @Composable
        get() = BarHeight + frnkBottomSystemBarInset()
}

/**
 * The toolkit's **platform-adaptive** bottom navigation bar: a genuine native UIKit `UITabBar` on iOS and
 * a Material3 `NavigationBar` on Android/Desktop, via [Calf](https://github.com/MohamedRejeb/Calf)'s
 * `AdaptiveNavigationBar`. Generic over [FrnkBottomNavItem] (icon + label); [onItemSelected] receives the
 * tapped index.
 *
 * This is the ONE toolkit component that intentionally renders through Material3 — the deliberate, approved
 * trade for true-native iOS chrome from a single component. For the icon-only floating pill that stays
 * pure-`compose-unstyled`, use `FrnkBottomNavBar` in `shared-ui-atoms` instead.
 *
 * Most hosts use [FrnkAdaptiveBottomNavScaffold], which owns tab selection and renders this bar; reach for
 * this lower-level composable directly only when you wire your own selected-tab state / navigation.
 */
@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun FrnkAdaptiveBottomNavBar(
    items: List<FrnkBottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Drive the native bars from FrnkTheme tokens so they follow the host's brand instead of each
    // platform's default tint (iOS UITabBar would otherwise show system blue; Material3 its baseline).
    val selectedColor = Theme[colors][colorPrimary]
    val unselectedColor = Theme[colors][colorOnSurfaceVariant]
    val indicatorColor = Theme[colors][colorPrimaryContainer]
    // Theme the bar's *surface* too — otherwise the Android Material3 NavigationBar falls back to
    // NavigationBarDefaults.containerColor (the unthemed Material baseline), so it ignores the
    // FrnkTheme light/dark palette and stays light in dark mode. (Calf's iOS UITabBar takes no
    // background token in 0.12.0 — it keeps its native translucent material, which is the desired
    // native look there.)
    val containerColor = Theme[colors][colorSurface]
    val onContainerColor = Theme[colors][colorOnSurface]
    AdaptiveNavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = onContainerColor,
        // iOS path: native UITabBar. Calf rasterises each ImageVector to a UIImage; the configuration maps
        // selected/unselected colors onto UITabBar.tintColor / unselectedItemTintColor.
        iosItems = items.map { UIKitUITabBarItem(title = it.label, image = UIKitImage.Vector(it.icon)) },
        iosSelectedIndex = selectedIndex,
        iosOnItemSelected = onItemSelected,
        iosConfiguration =
            UIKitTabBarConfiguration(
                selectedItemColor = selectedColor,
                unselectedItemColor = unselectedColor,
            ),
    ) {
        // Android/Desktop path: Material3 NavigationBarItems in the bar's RowScope, tinted from the tokens.
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onItemSelected(index) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        indicatorColor = indicatorColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                    ),
            )
        }
    }
}
