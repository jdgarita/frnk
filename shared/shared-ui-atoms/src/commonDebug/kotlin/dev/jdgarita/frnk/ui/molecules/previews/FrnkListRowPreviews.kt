package dev.jdgarita.frnk.ui.molecules.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkSkeleton
import dev.jdgarita.frnk.ui.atoms.previews.PreviewSurface
import dev.jdgarita.frnk.ui.molecules.FrnkListRow
import dev.jdgarita.frnk.ui.molecules.FrnkListRowState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.iconChevronRight
import dev.jdgarita.frnk.ui.theme.iconNotifications
import dev.jdgarita.frnk.ui.theme.icons

@Composable
private fun chevronTrailing() {
    FrnkIcon(
        state =
            FrnkIconState(
                imageVector = Theme[icons][iconChevronRight],
                contentDescription = null,
                tint = colorOnSurfaceVariant,
            ),
    )
}

@Preview
@Composable
private fun FrnkListRow_Variants_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkListRow(
            state =
                FrnkListRowState(
                    title = "Notifications",
                    subtitle = "Push, email and in-app alerts",
                    icon = FrnkIconState(Theme[icons][iconNotifications], contentDescription = null),
                ),
            onClick = {},
            trailing = { chevronTrailing() },
        )
        FrnkListRow(
            state = FrnkListRowState(title = "Title only"),
            onClick = {},
            trailing = { chevronTrailing() },
        )
        FrnkListRow(
            state =
                FrnkListRowState(
                    title = "Non-interactive row",
                    subtitle = "No onClick, no trailing",
                    icon = FrnkIconState(Theme[icons][iconNotifications], contentDescription = null),
                ),
        )
    }
}

@Preview
@Composable
private fun FrnkListRow_Skeleton_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkListRow(
            state =
                FrnkListRowState(
                    title = "Loading row",
                    subtitle = "Loading subtitle",
                    icon = FrnkIconState(Theme[icons][iconNotifications], contentDescription = null),
                    skeleton = FrnkSkeleton(enabled = true),
                ),
            onClick = {},
            trailing = { chevronTrailing() },
        )
    }
}

@Preview
@Composable
private fun FrnkListRow_Variants_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkListRow(
            state =
                FrnkListRowState(
                    title = "Notifications",
                    subtitle = "Push, email and in-app alerts",
                    icon = FrnkIconState(Theme[icons][iconNotifications], contentDescription = null),
                ),
            onClick = {},
            trailing = { chevronTrailing() },
        )
    }
}
