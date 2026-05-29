package dev.jdgarita.frnk.ui.atoms

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.ProvideContentColor
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.colorOnSurface
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorOutlineVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes

@Immutable
data class FrnkBottomNavItem(
    val key: String,
    val icon: ImageVector,
    /** Doubles as the icon's [contentDescription] — the bar itself is icon-only. */
    val label: String,
)

@Immutable
data class FrnkBottomNavBarState(
    val items: List<FrnkBottomNavItem>,
    val selectedIndex: Int,
    val enabled: Boolean = true,
)

// Pill geometry — design-specific layout constants (see `Still Bottom Nav - Component`). Kept as
// private `dp` vals rather than spacing tokens because the design pins exact pixel values, the same
// precedent as FrnkSwitch's TrackWidth/ThumbSize. Colors/shapes still resolve from Theme tokens.
private val ButtonSize = 48.dp
private val NavIconSize = 20.dp
private val PillInnerPadding = 6.dp
private val PillItemGap = 6.dp
private val PillElevation = 12.dp
private val WrapperHorizontalPadding = 16.dp
private val WrapperTopPadding = 10.dp
private val WrapperBottomPadding = 14.dp

// color-mix(in oklch, primary 14%, surface) → an RGB lerp; visually equivalent for a tint this light.
private const val ACTIVE_TINT_FRACTION = 0.14f

/** Shared layout metrics for the floating bottom-nav bar. */
object FrnkBottomNavBarDefaults {
    /**
     * Total vertical space the floating bar occupies (wrapper insets + pill height). Hosts that let
     * content scroll *behind* the bar should reserve this as bottom content padding so the last item
     * can settle just above the pill instead of being trapped under it — `BottomNavScaffoldContent`
     * passes exactly this through to its `tabContent` slot.
     */
    val BarHeight: Dp = WrapperTopPadding + PillInnerPadding * 2 + ButtonSize + WrapperBottomPadding
}

/**
 * Headless floating bottom-navigation bar — a centered, [colorSurface]-filled pill of icon-only tab
 * buttons that hovers over [colorBackground] (not pinned edge-to-edge). The selected tab fills with
 * a 14% [colorPrimary] tint and its icon switches to [colorPrimary]; idle tabs stay transparent with
 * a [colorOnSurfaceVariant] icon. Built on foundation primitives (no Material3); each glyph renders
 * through [FrnkIcon].
 *
 * Stateless and generic over [FrnkBottomNavBarState.items] — [onItemSelected] receives the tapped
 * index. For the toolkit's 3-tab (Home / configurable / Settings) product contract plus selected-tab
 * state, use the `BottomNavScaffold` that wraps this atom.
 *
 * Note: Lucide [ImageVector]s carry a baked stroke width, so the design's idle→active stroke bump
 * (1.6 → 2.0) is not reproducible here; "active" is conveyed by color, which is the dominant signal.
 */
@Composable
fun FrnkBottomNavBar(
    state: FrnkBottomNavBarState,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = state.selectedIndex.coerceIn(0, (state.items.size - 1).coerceAtLeast(0))
    Row(
        // The wrapper is intentionally transparent (only the pill is filled) so that when the bar
        // floats over scrollable content — e.g. inside BottomNavScaffoldContent — the content stays
        // visible around and behind the pill as it scrolls. Over a plain app background it looks
        // identical to a `colorBackground`-filled wrapper.
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    PaddingValues(
                        start = WrapperHorizontalPadding,
                        end = WrapperHorizontalPadding,
                        top = WrapperTopPadding,
                        bottom = WrapperBottomPadding,
                    ),
                ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier
                    .alpha(if (state.enabled) 1f else 0.4f)
                    .shadow(PillElevation, Theme[shapes][shapeFull], spotColor = Theme[colors][colorOnSurface])
                    .clip(Theme[shapes][shapeFull])
                    .background(Theme[colors][colorSurface])
                    .border(1.dp, Theme[colors][colorOutlineVariant], Theme[shapes][shapeFull])
                    .padding(PillInnerPadding),
            horizontalArrangement = Arrangement.spacedBy(PillItemGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            state.items.forEachIndexed { index, item ->
                val isSelected = index == selected
                val activeTint = lerp(Theme[colors][colorSurface], Theme[colors][colorPrimary], ACTIVE_TINT_FRACTION)
                // Idle tabs fade to the pill's own `colorSurface` rather than `Color.Transparent`.
                // `Color.Transparent` carries black RGB channels, so animating to/from it drags the
                // crossfade through a dark, muddy intermediate — a visible "flash" on both the
                // outgoing and incoming tab. Fading between two opaque colors keeps it clean, and
                // since the pill is `colorSurface`-filled the idle tab looks identical at rest.
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) activeTint else Theme[colors][colorSurface],
                    label = "nav_item_bg",
                )
                val iconTint by animateColorAsState(
                    targetValue = Theme[colors][if (isSelected) colorPrimary else colorOnSurfaceVariant],
                    label = "nav_item_tint",
                )
                Box(
                    modifier =
                        Modifier
                            .size(ButtonSize)
                            .clip(Theme[shapes][shapeFull])
                            .background(backgroundColor)
                            .selectable(
                                selected = isSelected,
                                enabled = state.enabled,
                                role = Role.Tab,
                                onClick = { onItemSelected(index) },
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Provide the animated tint as content color so FrnkIcon (no tint token) picks
                    // it up via LocalContentColor — the icon color crossfades in lock-step with the
                    // background tint.
                    ProvideContentColor(iconTint) {
                        FrnkIcon(
                            state =
                                FrnkIconState(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    size = NavIconSize,
                                ),
                        )
                    }
                }
            }
        }
    }
}
