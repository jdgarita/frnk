package dev.jdgarita.frnk.ui.molecules.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.previews.PreviewSurface
import dev.jdgarita.frnk.ui.molecules.FrnkListRow
import dev.jdgarita.frnk.ui.molecules.FrnkListRowState
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeAction
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeableState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.colorOnSuccess
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorSuccess
import dev.jdgarita.frnk.ui.theme.iconChevronRight
import dev.jdgarita.frnk.ui.theme.iconError
import dev.jdgarita.frnk.ui.theme.iconNotifications
import dev.jdgarita.frnk.ui.theme.iconRestore
import dev.jdgarita.frnk.ui.theme.icons

@Composable
private fun chevronTrailing() {
    FrnkIcon(
        state =
            FrnkIconState.Content(
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
                FrnkListRowState.Content(
                    title = "Notifications",
                    subtitle = "Push, email and in-app alerts",
                    icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null),
                ),
            onClick = {},
            trailing = { chevronTrailing() },
        )
        FrnkListRow(
            state = FrnkListRowState.Content(title = "Title only"),
            onClick = {},
            trailing = { chevronTrailing() },
        )
        FrnkListRow(
            state =
                FrnkListRowState.Content(
                    title = "Non-interactive row",
                    subtitle = "No onClick, no trailing",
                    icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null),
                ),
        )
    }
}

@Preview
@Composable
private fun FrnkListRow_Skeleton_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkListRow(
            state = FrnkListRowState.Skeleton,
            onClick = {},
            trailing = { chevronTrailing() },
        )
    }
}

@Preview
@Composable
private fun FrnkListRow_Swipe_Open_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkListRow(
            state =
                FrnkListRowState.Content(
                    title = "Project Apollo",
                    subtitle = "Swipe left to reveal actions",
                    icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null),
                ),
            onClick = {},
            swipe =
                FrnkSwipeableState(
                    rightActions =
                        listOf(
                            FrnkSwipeAction(
                                icon = FrnkIconState.Content(Theme[icons][iconError], contentDescription = "Delete"),
                                label = "Delete",
                            ),
                            FrnkSwipeAction(
                                icon = FrnkIconState.Content(Theme[icons][iconRestore], contentDescription = "Archive"),
                                containerColor = colorSuccess,
                                contentColor = colorOnSuccess,
                                label = "Archive",
                            ),
                        ),
                ),
        )
    }
}

@Preview
@Composable
private fun FrnkListRow_Variants_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkListRow(
            state =
                FrnkListRowState.Content(
                    title = "Notifications",
                    subtitle = "Push, email and in-app alerts",
                    icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null),
                ),
            onClick = {},
            trailing = { chevronTrailing() },
        )
    }
}
