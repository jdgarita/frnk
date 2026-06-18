package dev.jdgarita.frnk.ui.atoms.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.jdgarita.frnk.ui.atoms.FrnkButton
import dev.jdgarita.frnk.ui.atoms.FrnkButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkButtonVariant
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

@Preview
@Composable
private fun FrnkButton_Skeleton_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
            FrnkButton(
                state = FrnkButtonState.Skeleton,
                onClick = {}
            )
            FrnkButton(
                state = FrnkButtonState.Skeleton,
                onClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun FrnkButton_Variants_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
            FrnkButton(state = FrnkButtonState.Content(text = "Filled"), onClick = {})
            FrnkButton(
                state = FrnkButtonState.Content(text = "Outlined", variant = FrnkButtonVariant.Outlined),
                onClick = {}
            )
            FrnkButton(
                state = FrnkButtonState.Content(text = "Ghost", variant = FrnkButtonVariant.Ghost),
                onClick = {}
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
            FrnkButton(state = FrnkButtonState.Content(text = "Disabled", enabled = false), onClick = {})
            FrnkButton(
                state =
                    FrnkButtonState.Content(
                        text = "Disabled Outlined",
                        variant = FrnkButtonVariant.Outlined,
                        enabled = false
                    ),
                onClick = {}
            )
        }
    }
}

@Preview
@Composable
private fun FrnkButton_Variants_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
            FrnkButton(state = FrnkButtonState.Content(text = "Filled"), onClick = {})
            FrnkButton(
                state = FrnkButtonState.Content(text = "Outlined", variant = FrnkButtonVariant.Outlined),
                onClick = {}
            )
        }
    }
}