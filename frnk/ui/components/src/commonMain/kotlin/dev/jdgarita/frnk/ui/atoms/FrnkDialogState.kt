package dev.jdgarita.frnk.ui.atoms

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource

/**
 * View state for [FrnkDialog]: a tinted icon badge, an optional eyebrow, a title, optional body copy
 * and a row (or stack) of actions.
 *
 * State shape — **Category C** (single-state `data class`, no `Skeleton`): a dialog is raised with its
 * copy already known and never shows a loading placeholder. See the component-state taxonomy in
 * `docs/HOST_INTEGRATION.md` §9.
 */
@Immutable
data class FrnkDialogState(
    val title: FrnkStringSource,
    /** In list order; see [actionLayout]. An empty list leaves the dialog with no way out — don't. */
    val actions: List<FrnkDialogAction>,
    val variant: FrnkDialogVariant = FrnkDialogVariant.Accent,
    /** A short label above the title, drawn upper-case in the variant's tint. */
    val eyebrow: FrnkStringSource? = null,
    val body: FrnkStringSource? = null,
    /** The badge glyph; `null` uses the [variant]'s default (see [FrnkDialogVariant]). */
    val icon: FrnkIconSource? = null,
    val actionLayout: FrnkDialogActionLayout = FrnkDialogActionLayout.Row
)