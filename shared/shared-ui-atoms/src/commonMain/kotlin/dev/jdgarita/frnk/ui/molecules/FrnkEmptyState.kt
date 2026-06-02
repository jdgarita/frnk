package dev.jdgarita.frnk.ui.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import dev.jdgarita.frnk.ui.atoms.FrnkButton
import dev.jdgarita.frnk.ui.atoms.FrnkButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkButtonVariant
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

/**
 * View state for [FrnkEmptyState] — a centered icon + title + optional subtitle + optional action
 * button, composed from the [FrnkIcon], [FrnkText], and [FrnkButton] atoms. Use it for empty lists,
 * zero search results, or error placeholders.
 *
 * **No skeleton field — by design.** An empty state is a *terminal* (zero-content) state, not a
 * loading state: while content is loading you render a skeleton of the *eventual* content (a list of
 * [FrnkListRow]s, say), and only fall back to an empty state once loading has finished with nothing
 * to show. A skeleton *of* an empty state would never be displayed, so there is nothing to skeletonize.
 *
 * @property icon the illustrative glyph (caller picks size/tint, e.g. [FrnkIconState] with a large size).
 * @property title the headline (rendered centered as `Title`).
 * @property subtitle optional supporting line (rendered centered, muted); omitted when `null`.
 * @property actionLabel optional CTA label; the action button renders only when this **and** an
 *   `onActionClick` are both supplied.
 */
@Immutable
data class FrnkEmptyStateState(
    val icon: FrnkIconState,
    val title: String,
    val subtitle: String? = null,
    val actionLabel: String? = null,
)

/**
 * A centered empty/zero-state block. Supply [onActionClick] together with
 * [FrnkEmptyStateState.actionLabel] to show a CTA — the [FrnkButton] brings its own ripple + haptic.
 */
@Composable
fun FrnkEmptyState(
    state: FrnkEmptyStateState,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(FrnkSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FrnkSpacing.sm),
    ) {
        FrnkIcon(state = state.icon)
        FrnkText(state = FrnkTextState.Title(text = state.title, textAlign = TextAlign.Center))
        state.subtitle?.let {
            FrnkText(
                state =
                    FrnkTextState.Body(
                        text = it,
                        textAlign = TextAlign.Center,
                        color = colorOnSurfaceVariant,
                    ),
            )
        }
        if (state.actionLabel != null && onActionClick != null) {
            FrnkButton(
                state = FrnkButtonState(text = state.actionLabel, variant = FrnkButtonVariant.Filled),
                onClick = onActionClick,
                modifier = Modifier.padding(top = FrnkSpacing.xs),
            )
        }
    }
}
