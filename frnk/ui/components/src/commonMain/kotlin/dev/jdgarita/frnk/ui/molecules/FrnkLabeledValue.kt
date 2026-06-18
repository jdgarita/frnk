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
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingMd
import dev.jdgarita.frnk.ui.theme.spacingXxs

/** How a [FrnkLabeledValue] arranges its label and value. */
enum class FrnkLabeledValueOrientation {
    /** Label and value share a row; the value is pushed to the end (key/value pair). */
    Inline,

    /** Label sits above the value (caption + figure). */
    Stacked
}

/**
 * Sealed visual state for [FrnkLabeledValue] — a muted label paired with a value, composed from two
 * [FrnkText] atoms (for key/value detail rows or small stacked stats). [Content] holds the label,
 * value, and orientation; [Skeleton] (an `object`) renders label + value as placeholder bars.
 * Toolkit-standard sealed-state + `Skeleton`-object shape.
 */
sealed interface FrnkLabeledValueState {
    @Immutable
    data class Content(
        val label: String,
        val value: String,
        val orientation: FrnkLabeledValueOrientation = FrnkLabeledValueOrientation.Inline
    ) : FrnkLabeledValueState

    data object Skeleton : FrnkLabeledValueState
}

/** A label paired with a value, laid out per [FrnkLabeledValueState.Content.orientation]. */
@Composable
fun FrnkLabeledValue(
    state: FrnkLabeledValueState,
    modifier: Modifier = Modifier
) {
    val content =
        when (state) {
            is FrnkLabeledValueState.Content -> state
            FrnkLabeledValueState.Skeleton -> {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FrnkText(state = FrnkTextState.Skeleton, modifier = Modifier.weight(1f))
                    FrnkText(state = FrnkTextState.Skeleton)
                }
                return
            }
        }

    val label =
        FrnkTextState.BodySmall(text = content.label, color = colorOnSurfaceVariant)
    val value =
        FrnkTextState.TitleMedium(text = content.value)

    when (content.orientation) {
        FrnkLabeledValueOrientation.Inline ->
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrnkText(state = label, modifier = Modifier.weight(1f))
                FrnkText(state = value.copy(textAlign = TextAlign.End))
            }

        FrnkLabeledValueOrientation.Stacked ->
            Column(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingXxs])
            ) {
                FrnkText(state = label)
                FrnkText(state = value)
            }
    }
}