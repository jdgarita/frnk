package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.UnstyledButton
import com.composeunstyled.theme.Theme
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

/**
 * Interactive icon button. [contentDescription] is non-nullable so every call site supplies a
 * label readable by TalkBack/VoiceOver — an icon button without one is invisible to assistive
 * tech. Use the plain [FrnkIcon] (which accepts `null`) for decorative cases.
 */
@Immutable
data class FrnkIconButtonState(
    val imageVector: ImageVector,
    val contentDescription: String,
    val size: Dp = FrnkIconSize.md,
    val tint: ThemeToken<Color>? = null,
    val contentPadding: PaddingValues = PaddingValues(FrnkSpacing.sm),
    val enabled: Boolean = true,
)

@Composable
fun FrnkIconButton(
    state: FrnkIconButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    UnstyledButton(
        onClick = onClick,
        enabled = state.enabled,
        modifier =
            modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .clip(Theme[shapes][shapeFull]),
        contentPadding = state.contentPadding,
    ) {
        FrnkIcon(
            state =
                FrnkIconState(
                    imageVector = state.imageVector,
                    contentDescription = state.contentDescription,
                    size = state.size,
                    tint = state.tint,
                    // Match FrnkButton's 0.4f disabled treatment so the icon visibly dims when
                    // the button is inactive.
                    tintAlpha = if (state.enabled) 1f else 0.4f,
                ),
        )
    }
}
