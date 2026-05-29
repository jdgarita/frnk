package dev.jdgarita.frnk.ui.atoms

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.composeunstyled.LocalContentColor
import com.composeunstyled.UnstyledIcon
import com.composeunstyled.theme.Theme
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize

@Immutable
data class FrnkIconState(
    val imageVector: ImageVector,
    val contentDescription: String?,
    val size: Dp = FrnkIconSize.md,
    val tint: ThemeToken<Color>? = null,
    val tintAlpha: Float = 1f,
    val skeleton: FrnkSkeleton = FrnkSkeleton(),
)

@Composable
fun FrnkIcon(
    state: FrnkIconState,
    modifier: Modifier = Modifier,
) {
    val tintToken = state.tint
    val resolvedTint =
        (if (tintToken == null) LocalContentColor.current else Theme[colors][tintToken])
            .copy(alpha = state.tintAlpha)

    UnstyledIcon(
        imageVector = state.imageVector,
        contentDescription = state.contentDescription,
        tint = resolvedTint,
        modifier = modifier.size(state.size).frnkSkeleton(state.skeleton),
    )
}
