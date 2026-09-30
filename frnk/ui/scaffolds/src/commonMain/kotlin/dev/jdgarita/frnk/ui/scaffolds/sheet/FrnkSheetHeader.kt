package dev.jdgarita.frnk.ui.scaffolds.sheet

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.theme.bodyMedium
import dev.jdgarita.frnk.ui.theme.colorOnSurface
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.headlineSmall
import dev.jdgarita.frnk.ui.theme.labelSmall
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingSm
import dev.jdgarita.frnk.ui.theme.spacingXxl
import dev.jdgarita.frnk.ui.theme.spacingXxs

/**
 * The head of a modal sheet's content, in the two shapes sheets take, so every sheet opens the same
 * way under the grabber.
 *
 * **Leading** (the default): a `labelSmall` [eyebrow] in `colorPrimary`, a `headlineSmall` [title],
 * or both, at the [FrnkModalSheetDefaults.Gutter]. It keeps the sheet's ✕ corner clear (the close
 * reserve: the ✕'s end inset plus its 48dp box, kept whether or not a ✕ is up, so a title never
 * shifts when one arrives) and starts `spacingXxs` under the grabber's row, so its first line sits
 * level with the ✕'s glyph.
 *
 * **Centred** ([centered]): the [title] over a `colorOnSurfaceVariant` [body] capped at a caption's
 * measure, both centred, with a dialog-like inset above — sheets that read as prompts rather than
 * as screens.
 *
 * [above] is drawn between the top inset and the title, for a sheet that leads with a picture. The
 * header ends on its last line; what follows adds its own breath. A [title] that changes crossfades
 * in place rather than snapping; a fixed one never animates.
 */
@Composable
fun FrnkSheetHeader(
    modifier: Modifier = Modifier,
    title: String? = null,
    eyebrow: String? = null,
    body: String? = null,
    centered: Boolean = false,
    above: (@Composable () -> Unit)? = null
) {
    val textAlign = if (centered) TextAlign.Center else TextAlign.Start
    val gutter = FrnkModalSheetDefaults.Gutter
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = gutter,
                    end = if (centered) gutter else FrnkModalSheetDefaults.CloseInsetEnd + Theme[spacing][spacingXxl],
                    top = if (centered) CENTERED_TOP_INSET else Theme[spacing][spacingXxs]
                ),
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    ) {
        var gapAbove = 0.dp
        if (above != null) {
            above()
            gapAbove = Theme[spacing][spacingSm]
        }
        if (eyebrow != null) {
            FrnkText(
                state = FrnkTextState.Raw(text = eyebrow, style = labelSmall, color = colorPrimary, textAlign = textAlign),
                modifier = Modifier.padding(top = gapAbove)
            )
            gapAbove = Theme[spacing][spacingXxs]
        }
        if (title != null) {
            Crossfade(
                targetState = title,
                modifier = Modifier.padding(top = gapAbove).then(if (centered) Modifier.fillMaxWidth() else Modifier),
                animationSpec = tween(TITLE_FADE_MILLIS),
                label = "sheet-header-title"
            ) { shown ->
                FrnkText(
                    state = FrnkTextState.Raw(text = shown, style = headlineSmall, color = colorOnSurface, textAlign = textAlign),
                    modifier = if (centered) Modifier.fillMaxWidth() else Modifier
                )
            }
        }
        if (body != null) {
            FrnkText(
                state = FrnkTextState.Raw(text = body, style = bodyMedium, color = colorOnSurfaceVariant, textAlign = textAlign),
                modifier =
                    Modifier
                        .then(if (centered) Modifier.widthIn(max = CENTERED_BODY_MAX_WIDTH) else Modifier)
                        .padding(top = Theme[spacing][spacingSm])
            )
        }
    }
}

/** What the centred header keeps above its title, a dialog-like breath. */
private val CENTERED_TOP_INSET: Dp = 32.dp

/** The centred body's measure, so it reads as a caption under the title rather than a paragraph. */
private val CENTERED_BODY_MAX_WIDTH: Dp = 260.dp

private const val TITLE_FADE_MILLIS = 160