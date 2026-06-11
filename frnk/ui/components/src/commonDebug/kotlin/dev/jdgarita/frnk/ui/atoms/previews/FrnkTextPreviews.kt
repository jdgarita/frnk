package dev.jdgarita.frnk.ui.atoms.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkSkeleton
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colors

@Preview
@Composable
private fun FrnkText_Skeleton_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkText(state = FrnkTextState.Title(text = "Loading title", skeleton = FrnkSkeleton(enabled = true)))
        FrnkText(
            state = FrnkTextState.Body(text = "Loading a body line", skeleton = FrnkSkeleton(enabled = true)),
        )
    }
}

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
        FrnkText(state = FrnkTextState.Body(text = "Tinted body", color = colorPrimary))
    }
}

@Preview
@Composable
private fun FrnkText_AllVariants_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkText(state = FrnkTextState.HeadlineSmall(text = "HeadlineSmall"))
        FrnkText(state = FrnkTextState.Title(text = "Title"))
        FrnkText(state = FrnkTextState.Body(text = "Body in dark mode."))
        FrnkText(state = FrnkTextState.BodySmall(text = "Tinted small", color = colorPrimary))
    }
}

@Preview
@Composable
private fun FrnkText_AppName_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        val brand =
            buildAnnotatedString {
                pushStyle(SpanStyle(color = Theme[colors][colorPrimary], fontWeight = FontWeight.Black))
                append("Fr")
                pop()
                append("nk")
            }
        FrnkText(state = FrnkTextState.AppName(annotated = brand))
    }
}
