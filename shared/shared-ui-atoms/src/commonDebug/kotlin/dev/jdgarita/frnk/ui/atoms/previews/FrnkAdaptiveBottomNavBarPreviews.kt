package dev.jdgarita.frnk.ui.atoms.previews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.composables.icons.lucide.ChartNoAxesColumn
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import dev.jdgarita.frnk.ui.atoms.FrnkAdaptiveBottomNavBar
import dev.jdgarita.frnk.ui.atoms.FrnkAdaptiveBottomNavBarState
import dev.jdgarita.frnk.ui.atoms.FrnkAdaptiveNavStyle
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem
import dev.jdgarita.frnk.ui.theme.Appearance

private val navItems =
    listOf(
        FrnkBottomNavItem(key = "home", icon = Lucide.House, label = "Home"),
        FrnkBottomNavItem(key = "stats", icon = Lucide.ChartNoAxesColumn, label = "Stats"),
        FrnkBottomNavItem(key = "settings", icon = Lucide.Settings, label = "Settings"),
    )

// NOTE: previews pass no hazeState, so the iOS frost renders as its flat translucent fallback here (a
// static preview has no scrolling hazeSource to blur anyway). Run the demo to see the live blur sampling
// content. The shape, layout, hairline, icon+label and selected tint are all faithful in the preview.

@Preview
@Composable
private fun FrnkAdaptiveBottomNavBar_IosFrosted_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkAdaptiveBottomNavBar(
            state =
                FrnkAdaptiveBottomNavBarState(
                    items = navItems,
                    selectedIndex = 0,
                    style = FrnkAdaptiveNavStyle.IosFrostedBar,
                ),
            onItemSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun FrnkAdaptiveBottomNavBar_IosFrosted_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkAdaptiveBottomNavBar(
            state =
                FrnkAdaptiveBottomNavBarState(
                    items = navItems,
                    selectedIndex = 1,
                    style = FrnkAdaptiveNavStyle.IosFrostedBar,
                ),
            onItemSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun FrnkAdaptiveBottomNavBar_AndroidPill_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkAdaptiveBottomNavBar(
            state =
                FrnkAdaptiveBottomNavBarState(
                    items = navItems,
                    selectedIndex = 2,
                    style = FrnkAdaptiveNavStyle.AndroidFloatingPill,
                ),
            onItemSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
