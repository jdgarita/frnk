package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composeunstyled.theme.Theme
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.AdaptiveNavigationBar
import com.mohamedrejeb.calf.ui.navigation.UIKitTabBarConfiguration
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem
import dev.jdgarita.frnk.ui.theme.colorOnSurface
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorPrimaryContainer
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors

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
