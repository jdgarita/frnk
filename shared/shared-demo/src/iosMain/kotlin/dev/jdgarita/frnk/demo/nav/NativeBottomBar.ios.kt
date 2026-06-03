package dev.jdgarita.frnk.demo.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.UIKitView
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colors
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UITabBar
import platform.UIKit.UITabBarDelegateProtocol
import platform.UIKit.UITabBarItem
import platform.darwin.NSObject

/**
 * Real UIKit `UITabBar` via Compose `UIKitView` interop — SF Symbol glyphs, the system blur material, and
 * native selection. The brand tint comes from `FrnkTheme` (`colorPrimary` → `UIColor`); everything else
 * follows UIKit appearance. Taps round-trip through a `UITabBarDelegate` back into Compose state.
 */
@Composable
actual fun NativeBottomBar(
    items: List<FrnkBottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier,
) {
    // rememberUpdatedState so the (once-)remembered delegate always calls the latest callback.
    val onSelected = rememberUpdatedState(onItemSelected)
    val tabItems =
        remember(items) {
            items.mapIndexed { index, item ->
                UITabBarItem(
                    title = item.label,
                    image = UIImage.systemImageNamed(sfSymbolForLabel(item.label)),
                    tag = index.toLong(),
                )
            }
        }
    val delegate =
        remember {
            object : NSObject(), UITabBarDelegateProtocol {
                override fun tabBar(
                    tabBar: UITabBar,
                    didSelectItem: UITabBarItem,
                ) {
                    val index = tabBar.items?.indexOf(didSelectItem) ?: -1
                    if (index >= 0) onSelected.value(index)
                }
            }
        }
    val tint = Theme[colors][colorPrimary].toUIColor()
    UIKitView(
        factory = {
            UITabBar().apply {
                setItems(tabItems, animated = false)
                this.delegate = delegate
                this.tintColor = tint
                selectedItem = tabItems.getOrNull(selectedIndex)
            }
        },
        modifier = modifier,
        update = { bar ->
            bar.tintColor = tint
            bar.selectedItem = tabItems.getOrNull(selectedIndex)
        },
    )
}

// The demo's three tabs → SF Symbols, for the authentic native glyphs (the toolkit's Lucide ImageVectors
// would have to be rasterised to UIImages otherwise; for the spike a small by-label map is enough).
private fun sfSymbolForLabel(label: String): String =
    when {
        label.equals("Home", ignoreCase = true) -> "house.fill"
        label.contains("Component", ignoreCase = true) -> "square.grid.2x2.fill"
        label.equals("Settings", ignoreCase = true) -> "gearshape.fill"
        else -> "circle.fill"
    }

private fun Color.toUIColor(): UIColor =
    UIColor(red = red.toDouble(), green = green.toDouble(), blue = blue.toDouble(), alpha = alpha.toDouble())
