package dev.jdgarita.frnk.ui.atoms.ext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.composeunstyled.theme.ThemeToken
import dev.jdgarita.frnk.ui.atoms.FrnkDialogVariant
import dev.jdgarita.frnk.ui.theme.colorError
import dev.jdgarita.frnk.ui.theme.colorOnError
import dev.jdgarita.frnk.ui.theme.colorOnPrimary
import dev.jdgarita.frnk.ui.theme.colorOnSuccess
import dev.jdgarita.frnk.ui.theme.colorOnSurface
import dev.jdgarita.frnk.ui.theme.colorOnWarning
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorSuccess
import dev.jdgarita.frnk.ui.theme.colorSurface
import dev.jdgarita.frnk.ui.theme.colorWarning
import dev.jdgarita.frnk.ui.theme.iconCheck
import dev.jdgarita.frnk.ui.theme.iconDelete
import dev.jdgarita.frnk.ui.theme.iconInfo
import dev.jdgarita.frnk.ui.theme.iconWarning

/** The color token that tints this variant's badge, eyebrow and primary action. */
internal fun FrnkDialogVariant.tintToken(): ThemeToken<Color> =
    when (this) {
        FrnkDialogVariant.Accent -> colorPrimary
        FrnkDialogVariant.Destructive -> colorError
        FrnkDialogVariant.Warning -> colorWarning
        FrnkDialogVariant.Success -> colorSuccess
        FrnkDialogVariant.Neutral -> colorOnSurface
    }

/** The content color drawn on a [tintToken] fill (a primary action's label). */
internal fun FrnkDialogVariant.onTintToken(): ThemeToken<Color> =
    when (this) {
        FrnkDialogVariant.Accent -> colorOnPrimary
        FrnkDialogVariant.Destructive -> colorOnError
        FrnkDialogVariant.Warning -> colorOnWarning
        FrnkDialogVariant.Success -> colorOnSuccess
        FrnkDialogVariant.Neutral -> colorSurface
    }

/** The badge glyph used when the state names none. */
internal fun FrnkDialogVariant.defaultIconToken(): ThemeToken<ImageVector> =
    when (this) {
        FrnkDialogVariant.Accent -> iconInfo
        FrnkDialogVariant.Destructive -> iconDelete
        FrnkDialogVariant.Warning -> iconWarning
        FrnkDialogVariant.Success -> iconCheck
        FrnkDialogVariant.Neutral -> iconInfo
    }