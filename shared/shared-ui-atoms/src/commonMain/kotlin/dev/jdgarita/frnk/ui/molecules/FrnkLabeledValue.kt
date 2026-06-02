package dev.jdgarita.frnk.ui.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.jdgarita.frnk.ui.atoms.FrnkSkeleton
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

/** How a [FrnkLabeledValue] arranges its label and value. */
enum class FrnkLabeledValueOrientation {
    /** Label and value share a row; the value is pushed to the end (key/value pair). */
    Inline,

    /** Label sits above the value (caption + figure). */
    Stacked,
}

/**
 * View state for [FrnkLabeledValue] — a muted label paired with a value, composed from two
 * [FrnkText] atoms. Use it for key/value detail rows ("Plan / Pro", "Renews / Jun 2026") or small
 * stacked stats.
 *
 * @property label the descriptor (rendered as muted `BodySmall`).
 * @property value the value (rendered as `TitleMedium`).
 * @property orientation [Inline] (label start, value end) or [Stacked] (label over value).
 * @property skeleton loading placeholder. The label is treated as static chrome and stays visible;
 *   the **value** carries the skeleton, so only the figure collapses to a placeholder block while
 *   `enabled` (the natural loading shape for a key/value pair).
 */
@Immutable
data class FrnkLabeledValueState(
    val label: String,
    val value: String,
    val orientation: FrnkLabeledValueOrientation = FrnkLabeledValueOrientation.Inline,
    val skeleton: FrnkSkeleton = FrnkSkeleton(),
)

/** A label paired with a value, laid out per [FrnkLabeledValueState.orientation]. */
@Composable
fun FrnkLabeledValue(
    state: FrnkLabeledValueState,
    modifier: Modifier = Modifier,
) {
    val label =
        FrnkTextState.BodySmall(text = state.label, color = colorOnSurfaceVariant)
    val value =
        FrnkTextState.TitleMedium(text = state.value, skeleton = state.skeleton)

    when (state.orientation) {
        FrnkLabeledValueOrientation.Inline ->
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FrnkText(state = label, modifier = Modifier.weight(1f))
                FrnkText(state = value.copy(textAlign = TextAlign.End))
            }

        FrnkLabeledValueOrientation.Stacked ->
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(FrnkSpacing.xxs),
            ) {
                FrnkText(state = label)
                FrnkText(state = value)
            }
    }
}
