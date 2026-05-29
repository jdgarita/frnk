package dev.jdgarita.frnk.ui.atoms

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.composeunstyled.theme.Theme
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.placeholder.Fade
import dev.jdgarita.frnk.ui.placeholder.PlaceholderHighlight
import dev.jdgarita.frnk.ui.placeholder.Shimmer
import dev.jdgarita.frnk.ui.placeholder.placeholder
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colorSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.shapeFull
import dev.jdgarita.frnk.ui.theme.shapes

/**
 * The animated highlight that sweeps across a skeleton block. [Shimmer] is a moving gradient band;
 * [Fade] is a gentle opacity breathe; [None] draws a static block with no animation.
 */
enum class FrnkSkeletonHighlight { Shimmer, Fade, None }

/**
 * State-level descriptor for an atom's loading **skeleton** (placeholder) effect. Every atom `*State`
 * carries one; when [enabled] is true the atom draws an opaque block (sized to its own content) with
 * an animated highlight instead of its normal visuals, and interactive atoms stop responding to input.
 *
 * All visual properties resolve from theme tokens — atoms never hardcode skeleton colors or shapes —
 * so hosts restyle the skeleton through the same [FrnkThemeConfig][dev.jdgarita.frnk.ui.theme.FrnkThemeConfig]
 * override map as everything else.
 *
 * @property enabled whether the skeleton is shown. Defaults to `false` (normal rendering).
 * @property color the block fill color token. Defaults to `colorSurfaceVariant`.
 * @property highlightColor the sweep/breathe highlight color token. Defaults to `colorSurface`.
 * @property shape the block shape token. `null` (the default) means "use the atom's own natural shape"
 *   — each atom passes its own shape to `Modifier.frnkSkeleton`, so a plain `FrnkSkeleton(enabled = true)`
 *   already matches the component (e.g. a button skeleton is button-shaped). Set a token to override.
 * @property highlight which highlight animation to run.
 */
@Immutable
data class FrnkSkeleton(
    val enabled: Boolean = false,
    val color: ThemeToken<Color> = colorSurfaceVariant,
    val highlightColor: ThemeToken<Color> = colorSurface,
    val shape: ThemeToken<Shape>? = null,
    val highlight: FrnkSkeletonHighlight = FrnkSkeletonHighlight.Shimmer,
)

/**
 * Applies [skeleton] to this [Modifier] by resolving its theme tokens and delegating to the vendored
 * `Modifier.placeholder`. A no-op when the skeleton is disabled, so atoms can apply it unconditionally.
 *
 * [shape] is the atom's natural skeleton shape, used when [FrnkSkeleton.shape] is `null`. It defaults to
 * `shapeFull`; atoms with a different resting shape (e.g. `FrnkButton` → `shapeButton`) pass their own.
 */
@Composable
internal fun Modifier.frnkSkeleton(
    skeleton: FrnkSkeleton,
    shape: ThemeToken<Shape> = shapeFull,
): Modifier =
    if (!skeleton.enabled) {
        this
    } else {
        placeholder(
            enabled = true,
            color = Theme[colors][skeleton.color],
            shape = Theme[shapes][skeleton.shape ?: shape],
            highlight = resolveHighlight(skeleton),
        )
    }

@Composable
private fun resolveHighlight(skeleton: FrnkSkeleton): PlaceholderHighlight? {
    val highlightColor = Theme[colors][skeleton.highlightColor]
    return when (skeleton.highlight) {
        FrnkSkeletonHighlight.Shimmer -> Shimmer(highlightColor = highlightColor)
        FrnkSkeletonHighlight.Fade -> Fade(highlightColor = highlightColor, animationSpec = FadeSpec)
        FrnkSkeletonHighlight.None -> null
    }
}

private val FadeSpec =
    infiniteRepeatable<Float>(
        animation = tween(durationMillis = 600, delayMillis = 200),
        repeatMode = RepeatMode.Reverse,
    )
