package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.ui.ExperimentalCalfUiApi
import com.mohamedrejeb.calf.ui.navigation.AdaptiveNavigationBar
import com.mohamedrejeb.calf.ui.navigation.UIKitUITabBarItem
import com.mohamedrejeb.calf.ui.uikit.UIKitImage
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem

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
    AdaptiveNavigationBar(
        modifier = modifier,
        // iOS path: native UITabBar. Calf rasterises each ImageVector to a UIImage.
        iosItems = items.map { UIKitUITabBarItem(title = it.label, image = UIKitImage.Vector(it.icon)) },
        iosSelectedIndex = selectedIndex,
        iosOnItemSelected = onItemSelected,
    ) {
        // Android/Desktop path: Material3 NavigationBarItems in the bar's RowScope.
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onItemSelected(index) },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(text = item.label) },
            )
        }
    }
}
