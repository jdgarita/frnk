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
import dev.jdgarita.frnk.ui.theme.iconSizeMd
import dev.jdgarita.frnk.ui.theme.iconSizes
import dev.jdgarita.frnk.ui.theme.shapeFull

/**
 * Sealed visual state for [FrnkIcon]. [Content] renders the glyph; [Skeleton] renders a loading
 * placeholder block. Both [Content.size] and [Skeleton.size] default to `null` = the theme's icon-size
 * axis (`Theme[iconSizes][iconSizeMd]`), so a host's `iconSizeOverrides` apply to default-sized icons;
 * pass an explicit token (e.g. `Theme[iconSizes][iconSizeLg]`) for a specific size. Toolkit-standard
 * sealed-state + `Skeleton` shape — see docs/HOST_INTEGRATION.md §9 ("Component style guide").
 */
sealed interface FrnkIconState {
    @Immutable
    data class Content(
        val imageVector: ImageVector,
        val contentDescription: String?,
        val size: Dp? = null,
        val tint: ThemeToken<Color>? = null,
        val tintAlpha: Float = 1f,
    ) : FrnkIconState

    /**
     * Loading placeholder. [size] `null` uses the theme default icon size; pass the **eventual icon's
     * size** so the placeholder matches its footprint and the layout doesn't jump when content loads.
     */
    data class Skeleton(
        val size: Dp? = null,
    ) : FrnkIconState
}

@Composable
fun FrnkIcon(
    state: FrnkIconState,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is FrnkIconState.Content -> {
            val tintToken = state.tint
            val resolvedTint =
                (if (tintToken == null) LocalContentColor.current else Theme[colors][tintToken])
                    .copy(alpha = state.tintAlpha)
            UnstyledIcon(
                imageVector = state.imageVector,
                contentDescription = state.contentDescription,
                tint = resolvedTint,
                modifier = modifier.size(state.size ?: Theme[iconSizes][iconSizeMd]),
            )
        }

        is FrnkIconState.Skeleton ->
            FrnkSkeletonBox(modifier.size(state.size ?: Theme[iconSizes][iconSizeMd]), shape = shapeFull)
    }
}
