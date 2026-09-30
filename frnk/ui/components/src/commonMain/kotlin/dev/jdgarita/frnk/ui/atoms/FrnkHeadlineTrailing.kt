package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.ui.theme.FrnkStringSource

/** The corner slot of [FrnkHeadlineTopBar]. */
@Immutable
sealed interface FrnkHeadlineTrailing {
    /** A plain icon button; a tap reports its key through `onActionClick`. It never enters search. */
    @Immutable
    data class Action(
        val action: FrnkHeadlineAction
    ) : FrnkHeadlineTrailing

    /**
     * A search toggle. The host owns [isActive] and [query]: `onSearchOpen` asks to enter search,
     * `onSearchClose` to leave it, and `onSearchQueryChange` streams edits from the field.
     */
    @Immutable
    data class Search(
        val isActive: Boolean,
        val query: String,
        /** Label for the search glyph shown while idle. */
        val openContentDescription: FrnkStringSource,
        /** Label for the close glyph shown while searching. */
        val closeContentDescription: FrnkStringSource
    ) : FrnkHeadlineTrailing
}