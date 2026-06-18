package dev.jdgarita.frnk.ui.atoms.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.jdgarita.frnk.ui.atoms.FrnkSwitch
import dev.jdgarita.frnk.ui.atoms.FrnkSwitchState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

@Preview
@Composable
private fun FrnkSwitch_Skeleton_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkSwitch(
            state = FrnkSwitchState.Skeleton,
            onCheckedChange = {}
        )
    }
}

@Preview
@Composable
private fun FrnkSwitch_States_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md)) {
            FrnkSwitch(state = FrnkSwitchState.Content(checked = true), onCheckedChange = {})
            FrnkSwitch(state = FrnkSwitchState.Content(checked = false), onCheckedChange = {})
            FrnkSwitch(state = FrnkSwitchState.Content(checked = true, enabled = false), onCheckedChange = {})
            FrnkSwitch(state = FrnkSwitchState.Content(checked = false, enabled = false), onCheckedChange = {})
        }
    }
}

@Preview
@Composable
private fun FrnkSwitch_States_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md)) {
            FrnkSwitch(state = FrnkSwitchState.Content(checked = true), onCheckedChange = {})
            FrnkSwitch(state = FrnkSwitchState.Content(checked = false), onCheckedChange = {})
        }
    }
}