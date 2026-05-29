package dev.jdgarita.frnk.ui.atoms.previews

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.jdgarita.frnk.ui.atoms.FrnkSegmentedControl
import dev.jdgarita.frnk.ui.atoms.FrnkSegmentedControlState
import dev.jdgarita.frnk.ui.atoms.FrnkSkeleton
import dev.jdgarita.frnk.ui.theme.Appearance

private val themeOptions = listOf("System", "Light", "Dark")

@Preview
@Composable
private fun FrnkSegmentedControl_Skeleton_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkSegmentedControl(
            state =
                FrnkSegmentedControlState(
                    options = themeOptions,
                    selectedIndex = 0,
                    skeleton = FrnkSkeleton(enabled = true),
                ),
            onOptionSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun FrnkSegmentedControl_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkSegmentedControl(
            state = FrnkSegmentedControlState(options = themeOptions, selectedIndex = 0),
            onOptionSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
        FrnkSegmentedControl(
            state = FrnkSegmentedControlState(options = themeOptions, selectedIndex = 1),
            onOptionSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
        FrnkSegmentedControl(
            state = FrnkSegmentedControlState(options = themeOptions, selectedIndex = 2),
            onOptionSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun FrnkSegmentedControl_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkSegmentedControl(
            state = FrnkSegmentedControlState(options = themeOptions, selectedIndex = 1),
            onOptionSelected = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
