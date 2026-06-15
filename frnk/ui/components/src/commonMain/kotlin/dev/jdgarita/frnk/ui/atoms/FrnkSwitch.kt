package dev.jdgarita.frnk.ui.atoms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.haptics.HapticType
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.theme.colorOutline
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes

/**
 * Sealed visual state for [FrnkSwitch]. [Content] is the interactive toggle; [Skeleton] (an `object`)
 * is the track-shaped loading placeholder. Toolkit-standard sealed-state + `Skeleton`-object shape.
 */
sealed interface FrnkSwitchState {
    @Immutable
    data class Content(
        val checked: Boolean,
        val enabled: Boolean = true,
    ) : FrnkSwitchState

    data object Skeleton : FrnkSwitchState
}

private val TrackWidth = 44.dp
private val TrackHeight = 26.dp
private val ThumbSize = 20.dp
private val ThumbPadding = 3.dp

/**
 * Headless toggle switch built on foundation primitives — no Material3. The track animates between
 * [colorPrimary] (on) and [colorOutline] (off); the [colorSurface] thumb slides between the two
 * edges. Interaction is exposed through `Modifier.toggleable` with [Role.Switch] so TalkBack /
 * VoiceOver announce it correctly. Disabled state mirrors [FrnkButton]'s `0.4f` fade.
 */
@Composable
fun FrnkSwitch(
    state: FrnkSwitchState,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content =
        when (state) {
            is FrnkSwitchState.Content -> state
            FrnkSwitchState.Skeleton -> {
                FrnkSkeletonBox(modifier.size(width = TrackWidth, height = TrackHeight), shape = shapeFull)
                return
            }
        }

    val haptics = LocalFrnkHaptics.current
    val trackColor by animateColorAsState(
        targetValue = Theme[colors][if (content.checked) colorPrimary else colorOutline],
        label = "switch_track",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (content.checked) TrackWidth - ThumbSize - ThumbPadding else ThumbPadding,
        label = "switch_thumb",
    )

    Box(
        modifier =
            modifier
                .alpha(if (content.enabled) 1f else 0.4f)
                .size(width = TrackWidth, height = TrackHeight)
                .clip(Theme[shapes][shapeFull])
                .background(trackColor)
                .toggleable(
                    value = content.checked,
                    enabled = content.enabled,
                    role = Role.Switch,
                    onValueChange = {
                        // Convention: selection atoms fire on an actual change. For a switch every
                        // toggle *is* a change, so this is unconditional (multi-option atoms like
                        // FrnkSegmentedControl guard on `index != selected`).
                        haptics.perform(HapticType.Selection)
                        onCheckedChange(it)
                    },
                ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier =
                Modifier
                    .offset(x = thumbOffset)
                    .size(ThumbSize)
                    .clip(Theme[shapes][shapeFull])
                    .background(Theme[colors][colorSurface]),
        )
    }
}
