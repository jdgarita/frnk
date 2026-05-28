package dev.jdgarita.frnk.ui.atoms

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.colorOnSurface
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colorSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.labelLarge
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

@Immutable
data class FrnkSegmentedControlState(
    val options: List<String>,
    val selectedIndex: Int,
    val enabled: Boolean = true,
)

/**
 * Headless segmented control — a horizontal group of mutually exclusive options where the selected
 * segment fills with [colorSurface] against the [colorSurfaceVariant] track. Built on foundation
 * primitives (no Material3); labels render through [FrnkText] so they pick up the host typeface.
 *
 * Used by the settings theme row (System / Light / Dark), but generic enough for any small,
 * fixed set of choices. [onOptionSelected] receives the tapped index.
 */
@Composable
fun FrnkSegmentedControl(
    state: FrnkSegmentedControlState,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = state.selectedIndex.coerceIn(0, (state.options.size - 1).coerceAtLeast(0))
    Row(
        modifier =
            modifier
                .alpha(if (state.enabled) 1f else 0.4f)
                .clip(Theme[shapes][shapeFull])
                .background(Theme[colors][colorSurfaceVariant])
                .padding(FrnkSpacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        state.options.forEachIndexed { index, label ->
            val isSelected = index == selected
            val segmentColor by animateColorAsState(
                targetValue = if (isSelected) Theme[colors][colorSurface] else Color.Transparent,
                label = "segment_bg",
            )
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(Theme[shapes][shapeFull])
                        .background(segmentColor)
                        .clickable(enabled = state.enabled) { onOptionSelected(index) }
                        .padding(vertical = FrnkSpacing.xs),
                horizontalArrangement = Arrangement.Center,
            ) {
                FrnkText(
                    state =
                        FrnkTextState.Raw(
                            text = label,
                            style = labelLarge,
                            color = if (isSelected) colorOnSurface else colorOnSurfaceVariant,
                            textAlign = TextAlign.Center,
                            singleLine = true,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
