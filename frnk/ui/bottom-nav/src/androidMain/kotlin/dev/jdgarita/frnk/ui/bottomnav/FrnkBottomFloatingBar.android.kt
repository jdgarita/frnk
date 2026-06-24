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
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.ext.resolve

// Gap between the floating pill and the system navigation bar it hovers above.
private val PillBottomMargin = 12.dp

/**
 * **Android** [FrnkBottomFloatingBar] — a Material3 *Expressive* [HorizontalFloatingToolbar] rendered as a
 * floating pill, centered at the bottom. Each [FrnkNavBarItem] becomes an [IconButton] over the item's
 * [FrnkNavBarItem.icon] `ImageVector` (icon-only); the selected item tints to `colorPrimary`, idle items to
 * `colorOnSurfaceVariant`. The pill width animates via [animateContentSize] as items change.
 *
 * **No FAB (Mode B).** The toolkit's primary action is a permanent centered nav item injected by the scaffold,
 * so the bar always uses the **plain** toolbar overload (no `floatingActionButton` slot). This matters: the
 * `floatingActionButton`-slot overload reserves the docked-FAB gap on the right *even when the slot is
 * empty*, which would shift the pill left-of-centre. The plain overload keeps the pill horizontally centred.
 *
 * Colors come from `FrnkTheme` tokens, not `MaterialTheme` — `HorizontalFloatingToolbar` is the toolkit's
 * sole Material3 surface and is themed explicitly. This actual never touches `DrawableResource`.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
actual fun FrnkBottomFloatingBar(
    items: List<FrnkNavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier
) {
    val toolbarColors: FloatingToolbarColors =
        FloatingToolbarDefaults.standardFloatingToolbarColors(
            toolbarContainerColor = Theme[colors][colorSurface],
            toolbarContentColor = Theme[colors][colorOnSurfaceVariant]
        )

    // contentAlignment on the Box (below) handles centering; this only lifts the pill off the system nav.
    // NB: no `animateContentSize()` here — it clips its content to the animated bounds, which cut off the
    // pill's bottom-edge shadow (the shadow spreads beyond the toolbar's measured size).
    val toolbarModifier = Modifier.padding(bottom = frnkBottomSystemBarInset() + PillBottomMargin)

    // contentAlignment keeps the pill centered no matter how wide the incoming [modifier] is (the scaffold
    // passes a fillMaxWidth() overlay modifier).
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        HorizontalFloatingToolbar(
            expanded = true,
            colors = toolbarColors,
            modifier = toolbarModifier,
            // The no-FAB overload defaults to Level0 (0.dp) — no shadow — while the WithFab overload
            // defaults to Level1. Pin it to the WithFab elevation so the pill casts the standard shadow.
            expandedShadowElevation = FloatingToolbarDefaults.ContainerExpandedElevationWithFab
        ) {
            NavBarItems(items, selectedIndex, onItemSelected)
        }
    }
}

@Composable
private fun RowScope.NavBarItems(
    items: List<FrnkNavBarItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    items.forEachIndexed { index, item ->
        val selected = index == selectedIndex
        IconButton(onClick = { onItemSelected(index) }) {
            Icon(
                imageVector = item.icon.resolve(),
                contentDescription = item.label,
                tint = Theme[colors][if (selected) colorPrimary else colorOnSurfaceVariant]
            )
        }
    }
}