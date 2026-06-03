package dev.jdgarita.frnk.demo.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavBar
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavBarState
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem

/**
 * Android has no native `UITabBar`, so the "Native" variant falls back to the toolkit floating pill — the
 * platform-appropriate bar there. Keeps the demo's 4-way toggle renderable on Android.
 */
@Composable
actual fun NativeBottomBar(
    items: List<FrnkBottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier,
) {
    FrnkBottomNavBar(
        state = FrnkBottomNavBarState(items = items, selectedIndex = selectedIndex),
        onItemSelected = onItemSelected,
        modifier = modifier,
    )
}
