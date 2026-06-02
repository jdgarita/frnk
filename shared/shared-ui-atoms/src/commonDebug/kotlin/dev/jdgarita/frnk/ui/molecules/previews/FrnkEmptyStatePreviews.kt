package dev.jdgarita.frnk.ui.molecules.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.previews.PreviewSurface
import dev.jdgarita.frnk.ui.molecules.FrnkEmptyState
import dev.jdgarita.frnk.ui.molecules.FrnkEmptyStateState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.iconSearch
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize

@Preview
@Composable
private fun FrnkEmptyState_WithAction_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkEmptyState(
            state =
                FrnkEmptyStateState(
                    icon =
                        FrnkIconState(
                            imageVector = Theme[icons][iconSearch],
                            contentDescription = null,
                            size = FrnkIconSize.emptyState,
                            tint = colorOnSurfaceVariant,
                        ),
                    title = "No results",
                    subtitle = "Try adjusting your search to find what you're looking for.",
                    actionLabel = "Clear search",
                ),
            onActionClick = {},
        )
    }
}

@Preview
@Composable
private fun FrnkEmptyState_NoAction_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkEmptyState(
            state =
                FrnkEmptyStateState(
                    icon =
                        FrnkIconState(
                            imageVector = Theme[icons][iconSearch],
                            contentDescription = null,
                            size = FrnkIconSize.emptyState,
                            tint = colorOnSurfaceVariant,
                        ),
                    title = "Nothing here yet",
                ),
        )
    }
}

@Preview
@Composable
private fun FrnkEmptyState_WithAction_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkEmptyState(
            state =
                FrnkEmptyStateState(
                    icon =
                        FrnkIconState(
                            imageVector = Theme[icons][iconSearch],
                            contentDescription = null,
                            size = FrnkIconSize.emptyState,
                            tint = colorOnSurfaceVariant,
                        ),
                    title = "No results",
                    subtitle = "Try adjusting your search.",
                    actionLabel = "Clear search",
                ),
            onActionClick = {},
        )
    }
}
