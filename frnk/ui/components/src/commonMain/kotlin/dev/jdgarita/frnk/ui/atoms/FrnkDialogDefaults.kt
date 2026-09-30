package dev.jdgarita.frnk.ui.atoms

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Fixed measures of [FrnkDialog] that have no theme token. Colors, type, shapes and spacing are tokens. */
object FrnkDialogDefaults {
    /** The card's width cap; narrower windows get the full width minus the outer padding. */
    val MaxWidth: Dp = 320.dp

    /** Width cap for the body copy, so a long line wraps into a readable measure. */
    val BodyMaxWidth: Dp = 260.dp

    /** The square icon badge at the top of the card. */
    val IconBadgeSize: Dp = 48.dp

    /** The ring around the icon badge, in the variant's tint. */
    val IconBadgeBorderWidth: Dp = 1.5.dp

    /** The card's hairline border and the rule above the actions. */
    val HairlineWidth: Dp = 1.dp

    /** Minimum height of an action button (the touch-target size). */
    val ActionMinHeight: Dp = 48.dp
}