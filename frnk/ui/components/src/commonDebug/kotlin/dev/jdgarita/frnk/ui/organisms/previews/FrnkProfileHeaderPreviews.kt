package dev.jdgarita.frnk.ui.organisms.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.previews.PreviewSurface
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValueState
import dev.jdgarita.frnk.ui.organisms.FrnkProfileHeader
import dev.jdgarita.frnk.ui.organisms.FrnkProfileHeaderState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.colorOnPrimaryContainer
import dev.jdgarita.frnk.ui.theme.iconNavSettings
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize

private val sampleStats =
    listOf(
        FrnkLabeledValueState.Content(label = "Projects", value = "12"),
        FrnkLabeledValueState.Content(label = "Streak", value = "48d"),
        FrnkLabeledValueState.Content(label = "Plan", value = "Pro")
    )

@Composable
private fun avatar() =
    FrnkIconState.Content(
        imageVector = Theme[icons][iconNavSettings],
        contentDescription = null,
        size = FrnkIconSize.lg,
        tint = colorOnPrimaryContainer
    )

@Preview
@Composable
private fun FrnkProfileHeader_WithStats_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkProfileHeader(
            state =
                FrnkProfileHeaderState.Content(
                    name = "Juan Diego",
                    subtitle = "juandiego@example.com",
                    avatar = avatar(),
                    stats = sampleStats
                )
        )
    }
}

@Preview
@Composable
private fun FrnkProfileHeader_NoStats_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkProfileHeader(
            state =
                FrnkProfileHeaderState.Content(
                    name = "Juan Diego",
                    subtitle = "Free plan",
                    avatar = avatar()
                )
        )
    }
}

@Preview
@Composable
private fun FrnkProfileHeader_Skeleton_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkProfileHeader(state = FrnkProfileHeaderState.Skeleton)
    }
}