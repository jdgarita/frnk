package dev.jdgarita.frnk.ui.organisms

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkDivider
import dev.jdgarita.frnk.ui.atoms.FrnkDividerState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.molecules.FrnkListRow
import dev.jdgarita.frnk.ui.molecules.FrnkListRowState
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.labelMedium
import dev.jdgarita.frnk.ui.theme.labelSmall
import dev.jdgarita.frnk.ui.theme.shapeCard
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

/**
 * View state for [FrnkListSection] — an **organism**: an optional section title, a surface card that
 * stacks N [FrnkListRowState] rows (drawn as [FrnkListRow] molecules separated by [FrnkDivider]s), and
 * an optional footnote. Generalises the Settings section-card idiom into a reusable building block for
 * grouped lists (account rows, a menu, a detail screen).
 *
 * @property rows the rows of the card, top to bottom. Each is a full [FrnkListRowState] (icon, title,
 *   subtitle, trailing handled by the caller, and its own `skeleton` flag).
 * @property title optional uppercase section header above the card; omitted when `null`.
 * @property footnote optional muted caption below the card; omitted when `null`.
 *
 * **Skeleton: yes — carried by the rows.** A list section is content-bearing, but the skeleton lives
 * one tier down: enable `skeleton` on the individual [FrnkListRowState]s (each collapses to a block)
 * rather than adding a section-level flag, so a partially-loaded list can skeletonize per row. The
 * card chrome (title, surface, dividers) is static framing.
 */
@Immutable
data class FrnkListSectionState(
    val rows: List<FrnkListRowState>,
    val title: String? = null,
    val footnote: String? = null,
)

/**
 * A titled card grouping its [FrnkListSectionState.rows] as [FrnkListRow]s separated by dividers. Pass
 * [onRowClick] to make rows tappable (the index identifies the row) — each row brings its own ripple +
 * [HapticType.Click][dev.jdgarita.frnk.ui.haptics.HapticType], gated off while that row's skeleton
 * shows. [trailing] is an optional per-row slot (chevron, switch, badge…). The card
 * `animateContentSize()`s so rows appearing/disappearing settle smoothly instead of popping the layout.
 */
@Composable
fun FrnkListSection(
    state: FrnkListSectionState,
    modifier: Modifier = Modifier,
    onRowClick: ((index: Int) -> Unit)? = null,
    trailing: (@Composable (index: Int) -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(FrnkSpacing.xs),
    ) {
        state.title?.let { title ->
            FrnkText(
                state =
                    FrnkTextState.Raw(
                        text = title.uppercase(),
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
                    .animateContentSize(),
        ) {
            state.rows.forEachIndexed { index, row ->
                if (index > 0) FrnkDivider(state = FrnkDividerState.Horizontal())
                FrnkListRow(
                    state = row,
                    onClick = onRowClick?.let { { it(index) } },
                    trailing = trailing?.let { { it(index) } },
                )
            }
        }
        state.footnote?.let { footnote ->
            FrnkText(
                state =
                    FrnkTextState.Raw(
                        text = footnote,
                        style = labelSmall,
                        color = colorOnSurfaceVariant,
                    ),
            )
        }
    }
}
