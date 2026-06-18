package dev.jdgarita.frnk.ui.atoms

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.haptics.HapticType
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.theme.colorOnSurface
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colorSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.labelLarge
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingXs
import dev.jdgarita.frnk.ui.theme.spacingXxs

/**
 * Sealed visual state for [FrnkSegmentedControl]. [Content] is the interactive option group;
 * [Skeleton] (an `object`) is the track-shaped loading placeholder. Toolkit-standard sealed-state +
 * `Skeleton`-object shape.
 */
sealed interface FrnkSegmentedControlState {
    @Immutable
    data class Content(
        val options: List<String>,
        val selectedIndex: Int,
        val enabled: Boolean = true
    ) : FrnkSegmentedControlState

    data object Skeleton : FrnkSegmentedControlState
}

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
    modifier: Modifier = Modifier
) {
    val content =
        when (state) {
            is FrnkSegmentedControlState.Content -> state
            FrnkSegmentedControlState.Skeleton -> {
                FrnkSkeletonBox(modifier.fillMaxWidth().height(40.dp), shape = shapeFull)
                return
            }
        }

    val haptics = LocalFrnkHaptics.current
    val selected = content.selectedIndex.coerceIn(0, (content.options.size - 1).coerceAtLeast(0))
    Row(
        modifier =
            modifier
                .alpha(if (content.enabled) 1f else 0.4f)
                .clip(Theme[shapes][shapeFull])
                .background(Theme[colors][colorSurfaceVariant])
                .padding(Theme[spacing][spacingXxs]),
        horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingXxs]),
        verticalAlignment = Alignment.CenterVertically
    ) {
        content.options.forEachIndexed { index, label ->
            val isSelected = index == selected
            // Idle segments fade to the track's `colorSurfaceVariant` rather than `Color.Transparent`.
            // `Color.Transparent` carries black RGB channels, so animating to/from it drags the
            // crossfade through a dark, muddy intermediate — a visible "flash" on both the outgoing
            // and incoming segment. Fading between two opaque colors keeps it clean, and since the
            // track is `colorSurfaceVariant`-filled the idle segment looks identical at rest.
            val segmentColor by animateColorAsState(
                targetValue = if (isSelected) Theme[colors][colorSurface] else Theme[colors][colorSurfaceVariant],
                label = "segment_bg"
            )
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .clip(Theme[shapes][shapeFull])
                        .background(segmentColor)
                        .clickable(enabled = content.enabled) {
                            if (index != selected) haptics.perform(HapticType.Selection)
                            onOptionSelected(index)
                        }.padding(vertical = Theme[spacing][spacingXs]),
                horizontalArrangement = Arrangement.Center
            ) {
                FrnkText(
                    state =
                        FrnkTextState.Raw(
                            text = label,
                            style = labelLarge,
                            color = if (isSelected) colorOnSurface else colorOnSurfaceVariant,
                            textAlign = TextAlign.Center,
                            singleLine = true
                        ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}