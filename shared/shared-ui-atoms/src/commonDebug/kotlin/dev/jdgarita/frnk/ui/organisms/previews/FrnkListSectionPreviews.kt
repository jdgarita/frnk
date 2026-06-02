package dev.jdgarita.frnk.ui.organisms.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkSkeleton
import dev.jdgarita.frnk.ui.atoms.previews.PreviewSurface
import dev.jdgarita.frnk.ui.molecules.FrnkListRowState
import dev.jdgarita.frnk.ui.organisms.FrnkListSection
import dev.jdgarita.frnk.ui.organisms.FrnkListSectionState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.iconChevronRight
import dev.jdgarita.frnk.ui.theme.iconNotifications
import dev.jdgarita.frnk.ui.theme.iconSettings
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

private val accountRows: List<FrnkListRowState>
    @Composable get() =
        listOf(
            FrnkListRowState(
                title = "Notifications",
                subtitle = "Push, email and in-app alerts",
                icon = FrnkIconState(Theme[icons][iconNotifications], contentDescription = null),
            ),
            FrnkListRowState(
                title = "Preferences",
                subtitle = "Theme, language and units",
                icon = FrnkIconState(Theme[icons][iconSettings], contentDescription = null),
            ),
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
                    footnote = "Manage how you're notified across devices.",
                ),
            onRowClick = {},
            trailing = { chevronTrailing() },
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
            trailing = { chevronTrailing() },
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
                        accountRows.map { it.copy(skeleton = FrnkSkeleton(enabled = true)) },
                ),
        )
    }
}
