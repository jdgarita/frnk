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
private fun FrnkSwitch_States_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md)) {
            FrnkSwitch(state = FrnkSwitchState(checked = true), onCheckedChange = {})
            FrnkSwitch(state = FrnkSwitchState(checked = false), onCheckedChange = {})
            FrnkSwitch(state = FrnkSwitchState(checked = true, enabled = false), onCheckedChange = {})
            FrnkSwitch(state = FrnkSwitchState(checked = false, enabled = false), onCheckedChange = {})
        }
    }
}

@Preview
@Composable
private fun FrnkSwitch_States_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md)) {
            FrnkSwitch(state = FrnkSwitchState(checked = true), onCheckedChange = {})
            FrnkSwitch(state = FrnkSwitchState(checked = false), onCheckedChange = {})
        }
    }
}
