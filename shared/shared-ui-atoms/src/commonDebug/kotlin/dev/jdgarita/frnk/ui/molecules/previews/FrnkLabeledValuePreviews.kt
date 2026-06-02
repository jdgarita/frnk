package dev.jdgarita.frnk.ui.molecules.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.jdgarita.frnk.ui.atoms.FrnkSkeleton
import dev.jdgarita.frnk.ui.atoms.previews.PreviewSurface
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValue
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValueOrientation
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValueState
import dev.jdgarita.frnk.ui.theme.Appearance

@Preview
@Composable
private fun FrnkLabeledValue_Variants_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkLabeledValue(state = FrnkLabeledValueState(label = "Plan", value = "Pro"))
        FrnkLabeledValue(state = FrnkLabeledValueState(label = "Renews", value = "Jun 2026"))
        FrnkLabeledValue(
            state =
                FrnkLabeledValueState(
                    label = "Storage used",
                    value = "4.2 GB",
                    orientation = FrnkLabeledValueOrientation.Stacked,
                ),
        )
    }
}

@Preview
@Composable
private fun FrnkLabeledValue_Skeleton_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkLabeledValue(
            state =
                FrnkLabeledValueState(
                    label = "Plan",
                    value = "Loading",
                    skeleton = FrnkSkeleton(enabled = true),
                ),
        )
        FrnkLabeledValue(
            state =
                FrnkLabeledValueState(
                    label = "Storage used",
                    value = "Loading",
                    orientation = FrnkLabeledValueOrientation.Stacked,
                    skeleton = FrnkSkeleton(enabled = true),
                ),
        )
    }
}

@Preview
@Composable
private fun FrnkLabeledValue_Variants_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkLabeledValue(state = FrnkLabeledValueState(label = "Plan", value = "Pro"))
        FrnkLabeledValue(
            state =
                FrnkLabeledValueState(
                    label = "Storage used",
                    value = "4.2 GB",
                    orientation = FrnkLabeledValueOrientation.Stacked,
                ),
        )
    }
}
