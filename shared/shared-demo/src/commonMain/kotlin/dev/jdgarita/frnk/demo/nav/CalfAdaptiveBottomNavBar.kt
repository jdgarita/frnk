package dev.jdgarita.frnk.demo.nav

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
 * SPIKE (`spike/adaptive-bottom-nav`): demo-only wrapper over Calf's [AdaptiveNavigationBar], for the
 * A/B comparison against the Haze-based `FrnkAdaptiveBottomNavBar`. **Not a `Frnk*` atom** and **not in
 * `shared-ui-atoms`** — Calf hard-depends on Material3, which the toolkit forbids in shippable modules, so
 * this lives in `:shared-demo` only (never reaches `:shared` / `FrnkKit`).
 *
 * On iOS this renders a genuine native UIKit `UITabBar` (the `iosItems`/`iosSelectedIndex`/`iosOnItemSelected`
 * params; the [content] lambda is ignored on iOS). On Android/Desktop it renders a Material3 `NavigationBar`
 * via the [content] slot. Limitations surfaced by the spike (see docs/spikes/adaptive-bottom-nav.md):
 * the iOS UITabBar follows UIKit appearance — it does NOT pick up `FrnkTheme` tokens, `LocalFrnkHaptics`,
 * the collapsible-bars coordinator, or the Haze frost; and it rasterises the `ImageVector` to a `UIImage`.
 */
@OptIn(ExperimentalCalfUiApi::class)
@Composable
fun CalfAdaptiveBottomNavBar(
    items: List<FrnkBottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    AdaptiveNavigationBar(
        modifier = modifier,
        // iOS path: native UITabBar. Calf rasterises the ImageVector to a UIImage on iOS.
        iosItems = items.map { UIKitUITabBarItem(title = it.label, image = UIKitImage.Vector(it.icon)) },
        iosSelectedIndex = selectedIndex,
        iosOnItemSelected = onItemSelected,
    ) {
        // Material path (Android/Desktop): Material3 NavigationBarItems inside the bar's RowScope.
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
