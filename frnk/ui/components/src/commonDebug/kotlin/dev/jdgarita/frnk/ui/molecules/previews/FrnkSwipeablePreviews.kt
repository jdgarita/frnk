package dev.jdgarita.frnk.ui.molecules.previews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.atoms.previews.PreviewSurface
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeAction
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeBehavior
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeDirection
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeable
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeableState
import dev.jdgarita.frnk.ui.molecules.rememberFrnkSwipeController
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colorOnSuccess
import dev.jdgarita.frnk.ui.theme.colorSuccess
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.iconError
import dev.jdgarita.frnk.ui.theme.iconRestore
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

@Composable
private fun sampleCard(text: String) {
    // A plain opaque card stands in for any wrapped content (FrnkListRow, a custom row, etc.).
    FrnkText(state = FrnkTextState.TitleMedium(text = text))
}

@Composable
private fun deleteAction() =
    FrnkSwipeAction(
        icon = FrnkIconState.Content(Theme[icons][iconError], contentDescription = "Delete"),
        label = "Delete",
    )

@Composable
private fun archiveAction() =
    FrnkSwipeAction(
        icon = FrnkIconState.Content(Theme[icons][iconRestore], contentDescription = "Archive"),
        containerColor = colorSuccess,
        contentColor = colorOnSuccess,
        label = "Archive",
    )

@Preview
@Composable
private fun FrnkSwipeable_Reveal_Open_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        // Seed an opened side so the (gesture-driven) action panel is visible in a static preview.
        FrnkSwipeable(
            state =
                FrnkSwipeableState(
                    behavior = FrnkSwipeBehavior.Reveal,
                    rightActions = listOf(deleteAction(), archiveAction()),
                ),
            onAction = {},
            controller = rememberFrnkSwipeController(initialRevealed = FrnkSwipeDirection.Right),
        ) {
            FrnkText(
                modifier = Modifier.fillMaxWidth(),
                state = FrnkTextState.TitleMedium(text = "Revealed: delete + archive"),
            )
        }
    }
}

@Preview
@Composable
private fun FrnkSwipeable_Resting_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkSwipeable(
            state = FrnkSwipeableState(rightActions = listOf(deleteAction())),
            onAction = {},
        ) {
            sampleCard("Swipe left to reveal")
        }
    }
}

@Preview
@Composable
private fun FrnkSwipeable_Dismiss_Open_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkSwipeable(
            state =
                FrnkSwipeableState(
                    behavior = FrnkSwipeBehavior.Dismiss,
                    rightActions = listOf(deleteAction()),
                ),
            onAction = {},
            controller = rememberFrnkSwipeController(initialRevealed = FrnkSwipeDirection.Right),
        ) {
            FrnkText(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(Theme[colors][colorBackground])
                        .padding(FrnkSpacing.md),
                state = FrnkTextState.TitleMedium(text = "Swipe-to-delete (dismiss)"),
            )
        }
    }
}
