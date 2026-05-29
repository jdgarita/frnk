package dev.jdgarita.frnk.ui.atoms.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconButton
import dev.jdgarita.frnk.ui.atoms.FrnkIconButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkSkeleton
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.colorError
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.iconBack
import dev.jdgarita.frnk.ui.theme.iconCheck
import dev.jdgarita.frnk.ui.theme.iconClose
import dev.jdgarita.frnk.ui.theme.iconError
import dev.jdgarita.frnk.ui.theme.iconSearch
import dev.jdgarita.frnk.ui.theme.iconSettings
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

@Preview
@Composable
private fun FrnkIcon_Skeleton_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md)) {
            FrnkIcon(
                state =
                    FrnkIconState(
                        imageVector = Theme[icons][iconCheck],
                        contentDescription = null,
                        size = FrnkIconSize.lg,
                        tint = colorPrimary,
                        skeleton = FrnkSkeleton(enabled = true),
                    ),
            )
            FrnkIconButton(
                state =
                    FrnkIconButtonState(
                        imageVector = Theme[icons][iconSettings],
                        contentDescription = "Settings",
                        skeleton = FrnkSkeleton(enabled = true),
                    ),
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun FrnkIcon_Sizes_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md)) {
            FrnkIcon(
                state =
                    FrnkIconState(
                        imageVector = Theme[icons][iconCheck],
                        contentDescription = "xs",
                        size = FrnkIconSize.xs,
                        tint = colorPrimary,
                    ),
            )
            FrnkIcon(
                state =
                    FrnkIconState(
                        imageVector = Theme[icons][iconCheck],
                        contentDescription = "md",
                        size = FrnkIconSize.md,
                        tint = colorPrimary,
                    ),
            )
            FrnkIcon(
                state =
                    FrnkIconState(
                        imageVector = Theme[icons][iconCheck],
                        contentDescription = "xl",
                        size = FrnkIconSize.xl,
                        tint = colorPrimary,
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun FrnkIcon_Registry_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md)) {
            FrnkIcon(state = FrnkIconState(Theme[icons][iconBack], "back"))
            FrnkIcon(state = FrnkIconState(Theme[icons][iconClose], "close"))
            FrnkIcon(state = FrnkIconState(Theme[icons][iconSearch], "search"))
            FrnkIcon(state = FrnkIconState(Theme[icons][iconSettings], "settings"))
            FrnkIcon(state = FrnkIconState(Theme[icons][iconError], "error", tint = colorError))
        }
    }
}

@Preview
@Composable
private fun FrnkIconButton_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
            FrnkIconButton(
                state =
                    FrnkIconButtonState(
                        imageVector = Theme[icons][iconBack],
                        contentDescription = "Back",
                        tint = colorPrimary,
                    ),
                onClick = {},
            )
            FrnkIconButton(
                state =
                    FrnkIconButtonState(
                        imageVector = Theme[icons][iconSettings],
                        contentDescription = "Settings",
                    ),
                onClick = {},
            )
            FrnkIconButton(
                state =
                    FrnkIconButtonState(
                        imageVector = Theme[icons][iconClose],
                        contentDescription = "Disabled",
                        tint = colorPrimary,
                        enabled = false,
                    ),
                onClick = {},
            )
        }
    }
}
