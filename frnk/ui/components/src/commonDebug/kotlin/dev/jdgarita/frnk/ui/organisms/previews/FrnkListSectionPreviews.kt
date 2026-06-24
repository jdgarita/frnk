package dev.jdgarita.frnk.ui.organisms.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.previews.PreviewSurface
import dev.jdgarita.frnk.ui.molecules.FrnkListRowState
import dev.jdgarita.frnk.ui.organisms.FrnkListSection
import dev.jdgarita.frnk.ui.organisms.FrnkListSectionState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.iconChevronRight
import dev.jdgarita.frnk.ui.theme.iconNavSettings
import dev.jdgarita.frnk.ui.theme.iconNotifications
import dev.jdgarita.frnk.ui.theme.icons

@Composable
private fun chevronTrailing() {
    FrnkIcon(
        state =
            FrnkIconState.Content(
                imageVector = Theme[icons][iconChevronRight],
                contentDescription = null,
                tint = colorOnSurfaceVariant
            )
    )
}

private val accountRows: List<FrnkListRowState>
    @Composable get() =
        listOf(
            FrnkListRowState.Content(
                title = "Notifications",
                subtitle = "Push, email and in-app alerts",
                icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null)
            ),
            FrnkListRowState.Content(
                title = "Preferences",
                subtitle = "Theme, language and units",
                icon = FrnkIconState.Content(Theme[icons][iconNavSettings], contentDescription = null)
            )
        )

@Preview
@Composable
private fun FrnkListSection_Titled_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkListSection(
            state =
                FrnkListSectionState(
                    title = "Account",
                    rows = accountRows,
                    footnote = "Manage how you're notified across devices."
                ),
            onRowClick = {},
            trailing = { chevronTrailing() }
        )
    }
}

@Preview
@Composable
private fun FrnkListSection_Untitled_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkListSection(
            state = FrnkListSectionState(rows = accountRows),
            onRowClick = {},
            trailing = { chevronTrailing() }
        )
    }
}

@Preview
@Composable
private fun FrnkListSection_Skeleton_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkListSection(
            state =
                FrnkListSectionState(
                    title = "Account",
                    rows =
                        List(3) { FrnkListRowState.Skeleton }
                )
        )
    }
}