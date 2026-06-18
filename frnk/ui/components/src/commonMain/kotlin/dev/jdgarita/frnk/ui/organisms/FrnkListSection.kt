package dev.jdgarita.frnk.ui.organisms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import dev.jdgarita.frnk.ui.atoms.FrnkDivider
import dev.jdgarita.frnk.ui.molecules.FrnkListRow
import dev.jdgarita.frnk.ui.molecules.FrnkListRowState

/**
 * View state for [FrnkListSection] — an **organism**: an optional section title, a surface card that
 * stacks N [FrnkListRowState] rows (drawn as [FrnkListRow] molecules separated by [FrnkDivider]s), and
 * an optional footnote. Generalises the Settings section-card idiom into a reusable building block for
 * grouped lists (account rows, a menu, a detail screen).
 *
 * @property rows the rows of the card, top to bottom. Each is a full [FrnkListRowState] (icon, title,
 *   subtitle, trailing handled by the caller, and its own `skeleton` flag). Expected non-empty — for
 *   the zero-content case render a [FrnkEmptyState][dev.jdgarita.frnk.ui.molecules.FrnkEmptyState]
 *   rather than an empty section (an empty list paints an empty card shell).
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
    val footnote: String? = null
)

/**
 * A titled card grouping its [FrnkListSectionState.rows] as [FrnkListRow]s separated by dividers. Pass
 * [onRowClick] to make rows tappable (the index identifies the row) — each row brings its own ripple +
 * [HapticType.Click][dev.jdgarita.frnk.ui.haptics.HapticType], gated off while that row's skeleton
 * shows. [trailing] is an optional per-row slot (chevron, switch, badge…). The card chrome
 * (`animateContentSize()`, dividers, surface) is shared with the Settings scaffold via [FrnkSectionCard].
 */
@Composable
fun FrnkListSection(
    state: FrnkListSectionState,
    modifier: Modifier = Modifier,
    onRowClick: ((index: Int) -> Unit)? = null,
    trailing: (@Composable (index: Int) -> Unit)? = null
) {
    FrnkSectionCard(
        rows = state.rows,
        modifier = modifier,
        title = state.title,
        footnote = state.footnote
    ) { index, row ->
        FrnkListRow(
            state = row,
            onClick = onRowClick?.let { { it(index) } },
            trailing = trailing?.let { { it(index) } }
        )
    }
}