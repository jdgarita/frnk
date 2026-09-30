package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.ui.theme.FrnkStringSource

/** One button in a [FrnkDialog]'s action area; a tap hands this action back through `onAction`. */
@Immutable
data class FrnkDialogAction(
    /** Stable identifier the host switches on in `onAction`. */
    val key: String,
    /** The button label; resolved at the dialog, so tokens localize. */
    val label: FrnkStringSource,
    val kind: FrnkDialogActionKind
)