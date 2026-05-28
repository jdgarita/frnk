package dev.jdgarita.frnk.ui.atoms.previews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.composables.icons.lucide.ChartNoAxesColumn
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavBar
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavBarState
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem
import dev.jdgarita.frnk.ui.theme.Appearance

private val navItems =
    listOf(
        FrnkBottomNavItem(key = "home", icon = Lucide.House, label = "Home"),
        FrnkBottomNavItem(key = "stats", icon = Lucide.ChartNoAxesColumn, label = "Stats"),
        FrnkBottomNavItem(key = "settings", icon = Lucide.Settings, label = "Settings"),
    )

@Preview
@Composable
private fun FrnkBottomNavBar_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkBottomNavBar(
            state = FrnkBottomNavBarState(items = navItems, selectedIndex = 0),
            onItemSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
        FrnkBottomNavBar(
            state = FrnkBottomNavBarState(items = navItems, selectedIndex = 1),
            onItemSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
        FrnkBottomNavBar(
            state = FrnkBottomNavBarState(items = navItems, selectedIndex = 2),
            onItemSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun FrnkBottomNavBar_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkBottomNavBar(
            state = FrnkBottomNavBarState(items = navItems, selectedIndex = 1),
            onItemSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
