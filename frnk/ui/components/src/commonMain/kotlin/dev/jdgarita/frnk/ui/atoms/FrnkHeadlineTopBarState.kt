package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.ui.theme.FrnkStringSource

/**
 * View state for [FrnkHeadlineTopBar]: a large headline title between a [leading] action and a
 * [trailing] corner slot, with an optional [badge] and [actions] that fold away while searching.
 *
 * State shape — **Category C** (single-state `data class`, no `Skeleton`): searching is a flag on the
 * [FrnkHeadlineTrailing.Search] slot, not a separate state, and the bar never shows a loading
 * placeholder. See the component-state taxonomy in `docs/HOST_INTEGRATION.md` §9.
 */
@Immutable
data class FrnkHeadlineTopBarState(
    /** The headline; also the search field's placeholder while searching. */
    val title: FrnkStringSource,
    /** Always shown; disabled while searching (e.g. a settings gear). */
    val leading: FrnkHeadlineAction,
    /** Shown only when non-null and not searching (e.g. an upgrade pill for non-Pro users). */
    val badge: FrnkHeadlineBadge? = null,
    /** Folded away while searching (e.g. an add button). */
    val actions: List<FrnkHeadlineAction> = emptyList(),
    /** The corner slot: a search toggle, or a plain action that never searches. */
    val trailing: FrnkHeadlineTrailing
)