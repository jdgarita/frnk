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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.theme.colorOutline
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes

@Immutable
data class FrnkSwitchState(
    val checked: Boolean,
    val enabled: Boolean = true,
    val skeleton: FrnkSkeleton = FrnkSkeleton(),
)

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
    val trackColor by animateColorAsState(
        targetValue = Theme[colors][if (state.checked) colorPrimary else colorOutline],
        label = "switch_track",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (state.checked) TrackWidth - ThumbSize - ThumbPadding else ThumbPadding,
        label = "switch_thumb",
    )

    Box(
        modifier =
            modifier
                .alpha(if (state.enabled) 1f else 0.4f)
                .size(width = TrackWidth, height = TrackHeight)
                .clip(Theme[shapes][shapeFull])
                // When the skeleton is on, the placeholder block fully covers the track, but its
                // antialiased rim sits ~1px inside the clip — enough to reveal a hairline of the brand
                // track color underneath. Drop the track fill so there's nothing to peek through.
                .background(if (state.skeleton.enabled) Color.Transparent else trackColor)
                .frnkSkeleton(state.skeleton)
                .toggleable(
                    value = state.checked,
                    enabled = state.enabled && !state.skeleton.enabled,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
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
