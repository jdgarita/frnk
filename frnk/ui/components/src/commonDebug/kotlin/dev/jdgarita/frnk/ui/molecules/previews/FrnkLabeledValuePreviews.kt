package dev.jdgarita.frnk.ui.molecules.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.jdgarita.frnk.ui.atoms.previews.PreviewSurface
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValue
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValueOrientation
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValueState
import dev.jdgarita.frnk.ui.theme.Appearance

@Preview
@Composable
private fun FrnkLabeledValue_Variants_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkLabeledValue(state = FrnkLabeledValueState.Content(label = "Plan", value = "Pro"))
        FrnkLabeledValue(state = FrnkLabeledValueState.Content(label = "Renews", value = "Jun 2026"))
        FrnkLabeledValue(
            state =
                FrnkLabeledValueState.Content(
                    label = "Storage used",
                    value = "4.2 GB",
                    orientation = FrnkLabeledValueOrientation.Stacked
                )
        )
    }
}

@Preview
@Composable
private fun FrnkLabeledValue_Skeleton_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkLabeledValue(state = FrnkLabeledValueState.Skeleton)
        FrnkLabeledValue(state = FrnkLabeledValueState.Skeleton)
    }
}

@Preview
@Composable
private fun FrnkLabeledValue_Variants_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkLabeledValue(state = FrnkLabeledValueState.Content(label = "Plan", value = "Pro"))
        FrnkLabeledValue(
            state =
                FrnkLabeledValueState.Content(
                    label = "Storage used",
                    value = "4.2 GB",
                    orientation = FrnkLabeledValueOrientation.Stacked
                )
        )
    }
}