package dev.jdgarita.frnk.ui.organisms

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkDivider
import dev.jdgarita.frnk.ui.atoms.FrnkDividerState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.labelMedium
import dev.jdgarita.frnk.ui.theme.labelSmall
import dev.jdgarita.frnk.ui.theme.shapeCard
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

/**
 * Shared section-card chrome: an optional uppercase [title] over a `shapeCard`/`colorSurface` card
 * that stacks [rows] separated by [FrnkDivider]s (`animateContentSize()` so a row appearing/
 * disappearing settles smoothly), with an optional [footnote] caption below. Each row is rendered by
 * the [row] slot, which receives the item's `index` (for per-row callbacks) and the item itself.
 *
 * This is the single source of the titled-card layout shared by the public [FrnkListSection] organism
 * (homogeneous [FrnkListRow][dev.jdgarita.frnk.ui.molecules.FrnkListRow] rows) and the Settings
 * scaffold's private `SettingsSection` (a heterogeneous sealed row hierarchy) — the card chrome lives
 * here once; only the row rendering differs per caller. `internal` because hosts compose sections via
 * those higher-level entry points, not this primitive directly.
 *
 * Caller contract: [rows] is expected non-empty. An empty list still paints the surface/title/footnote
 * chrome around zero rows (a near-zero-height card), which reads as a stray gap — render a
 * [FrnkEmptyState][dev.jdgarita.frnk.ui.molecules.FrnkEmptyState] for the zero-content case instead.
 */
@Composable
internal fun <T> FrnkSectionCard(
    rows: List<T>,
    modifier: Modifier = Modifier,
    title: String? = null,
    footnote: String? = null,
    row: @Composable (index: Int, item: T) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FrnkSpacing.xs),
    ) {
        title?.let {
            FrnkText(
                state =
                    FrnkTextState.Raw(
                        text = it.uppercase(),
                        style = labelMedium,
                        color = colorOnSurfaceVariant,
                    ),
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(Theme[shapes][shapeCard])
                    .background(Theme[colors][colorSurface])
                    // Animate height when rows are added/removed (e.g. the Subscription section
                    // swapping Free → Pro) so the change settles smoothly instead of popping.
                    .animateContentSize(),
        ) {
            rows.forEachIndexed { index, item ->
                if (index > 0) FrnkDivider(state = FrnkDividerState.Horizontal())
                row(index, item)
            }
        }
        footnote?.let {
            FrnkText(
                state =
                    FrnkTextState.Raw(
                        text = it,
                        style = labelSmall,
                        color = colorOnSurfaceVariant,
                    ),
            )
        }
    }
}
