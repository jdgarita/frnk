package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource

/** An icon button in [FrnkHeadlineTopBar]; a tap reports [key] through `onActionClick`. */
@Immutable
data class FrnkHeadlineAction(
    /** Stable identifier the host switches on in `onActionClick`. */
    val key: String,
    val icon: FrnkIconSource,
    /** The icon button's accessibility label; resolved at the bar, so tokens localize. */
    val contentDescription: FrnkStringSource,
    val enabled: Boolean = true
)