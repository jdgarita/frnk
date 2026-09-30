package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.ui.theme.FrnkStringSource

/**
 * A tappable text pill in [FrnkHeadlineTopBar] (e.g. "PRO"); a tap reports [key] through
 * `onActionClick`. A text pill rather than an icon, because frnk icons are always tinted.
 */
@Immutable
data class FrnkHeadlineBadge(
    /** Stable identifier the host switches on in `onActionClick`. */
    val key: String,
    /** The pill's visible text. */
    val label: FrnkStringSource,
    /** The pill's accessibility label, which says what a tap does (e.g. "Upgrade to Pro"). */
    val contentDescription: FrnkStringSource
)