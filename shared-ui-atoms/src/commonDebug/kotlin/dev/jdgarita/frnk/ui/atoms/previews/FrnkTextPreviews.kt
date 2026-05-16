package dev.jdgarita.frnk.ui.atoms.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.primary

@Preview
@Composable
private fun FrnkText_AllVariants_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkText(state = FrnkTextState.HeadlineSmall(text = "HeadlineSmall"))
        FrnkText(state = FrnkTextState.Title(text = "Title"))
        FrnkText(state = FrnkTextState.TitleMedium(text = "TitleMedium"))
        FrnkText(state = FrnkTextState.Body(text = "Body — primary reading style."))
        FrnkText(state = FrnkTextState.BodyMedium(text = "BodyMedium"))
        FrnkText(state = FrnkTextState.BodySmall(text = "BodySmall"))
        FrnkText(state = FrnkTextState.Body(text = "Tinted body", color = primary))
    }
}

@Preview
@Composable
private fun FrnkText_AllVariants_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkText(state = FrnkTextState.HeadlineSmall(text = "HeadlineSmall"))
        FrnkText(state = FrnkTextState.Title(text = "Title"))
        FrnkText(state = FrnkTextState.Body(text = "Body in dark mode."))
        FrnkText(state = FrnkTextState.BodySmall(text = "Tinted small", color = primary))
    }
}
