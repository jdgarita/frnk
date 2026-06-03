package dev.jdgarita.frnk.demo.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem

/**
 * SPIKE (`spike/adaptive-bottom-nav`): the *genuinely native* bottom bar — a real UIKit `UITabBar` on iOS
 * (via Compose `UIKitView` interop), and the toolkit floating pill on Android (a native tab bar is
 * meaningless there). This is the "Option A" candidate: true-native iOS chrome **without** Material3
 * (unlike Calf, which is a UITabBar but drags in Material3).
 *
 * Demo-only by design: UIKit interop / `expect`-`actual` has no place in `shared-ui-atoms` (which is
 * `commonMain`-only and platform-agnostic), so it lives here in `:shared-demo` to be evaluated against the
 * Haze Compose imitation. If it wins, the production home would be a dedicated module, not the atoms layer.
 */
@Composable
expect fun NativeBottomBar(
    items: List<FrnkBottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier,
)
