package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.frnkBottomSystemBarInset
import dev.jdgarita.frnk.ui.theme.colorOnPrimary
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors

// Gap between the floating pill and the system navigation bar it hovers above.
private val PillBottomMargin = 12.dp

/**
 * **Android** [FrnkBottomNavBar] — a Material3 *Expressive* [HorizontalFloatingToolbar] rendered as a
 * floating pill, centered at the bottom. Each [FrnkNavBarItem] becomes an [IconButton] over the item's
 * [FrnkNavBarItem.icon] `ImageVector` (icon-only, MVP); the selected item tints to `colorPrimary`, idle
 * items to `colorOnSurfaceVariant`. The primary-action button, when wired, is the toolbar's built-in
 * docked FAB (the `floatingActionButton` slot), so this module needs no separately-docked FAB.
 *
 * Colors come from `FrnkTheme` tokens, not `MaterialTheme` — `HorizontalFloatingToolbar` is the toolkit's
 * sole Material3 surface and is themed explicitly. This actual never touches `DrawableResource`.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun FrnkBottomNavBar(
    items: List<FrnkNavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier,
    primaryAction: FrnkNavPrimaryAction?,
    onPrimaryAction: (() -> Unit)?,
) {
    val toolbarColors: FloatingToolbarColors =
        FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = Theme[colors][colorSurface],
            toolbarContentColor = Theme[colors][colorOnSurfaceVariant],
            fabContainerColor = Theme[colors][colorPrimary],
            fabContentColor = Theme[colors][colorOnPrimary],
        )

    // contentAlignment on the Box (below) handles centering; this only lifts the pill off the system nav.
    val toolbarModifier = Modifier.padding(bottom = frnkBottomSystemBarInset() + PillBottomMargin)

    // contentAlignment keeps the pill centered no matter how wide the incoming [modifier] is (the scaffold
    // passes a fillMaxWidth() overlay modifier).
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        if (primaryAction != null && onPrimaryAction != null) {
            HorizontalFloatingToolbar(
                expanded = true,
                floatingActionButton = {
                    FloatingToolbarDefaults.StandardFloatingActionButton(onClick = onPrimaryAction) {
                        Icon(imageVector = primaryAction.icon, contentDescription = primaryAction.label)
                    }
                },
                colors = toolbarColors,
                modifier = toolbarModifier,
            ) {
                NavBarItems(items, selectedIndex, onItemSelected)
            }
        } else {
            HorizontalFloatingToolbar(
                expanded = true,
                colors = toolbarColors,
                modifier = toolbarModifier,
                // The no-FAB overload defaults to Level0 (0.dp) — no shadow — while the WithFab overload
                // defaults to Level1. Pin it to the WithFab elevation so the pill casts the same shadow on
                // every screen, whether or not a primary-action FAB is wired.
                expandedShadowElevation = FloatingToolbarDefaults.ContainerExpandedElevationWithFab,
            ) {
                NavBarItems(items, selectedIndex, onItemSelected)
            }
        }
    }
}

@Composable
private fun RowScope.NavBarItems(
    items: List<FrnkNavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
) {
    items.forEachIndexed { index, item ->
        val selected = index == selectedIndex
        IconButton(onClick = { onItemSelected(index) }) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = Theme[colors][if (selected) colorPrimary else colorOnSurfaceVariant],
            )
        }
    }
}
