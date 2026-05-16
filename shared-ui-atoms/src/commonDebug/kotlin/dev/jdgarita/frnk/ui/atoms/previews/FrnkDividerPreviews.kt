package dev.jdgarita.frnk.ui.atoms.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jdgarita.frnk.ui.atoms.FrnkDivider
import dev.jdgarita.frnk.ui.atoms.FrnkDividerState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.primary
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing

@Preview
@Composable
private fun FrnkDivider_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkText(state = FrnkTextState.Body(text = "Above divider"))
        FrnkDivider(state = FrnkDividerState.Horizontal())
        FrnkText(state = FrnkTextState.Body(text = "Below divider"))
        FrnkDivider(state = FrnkDividerState.Horizontal(thickness = 2.dp, color = primary))
        Row(
            modifier = Modifier.height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm),
        ) {
            FrnkText(state = FrnkTextState.Body(text = "Left"))
            FrnkDivider(state = FrnkDividerState.Vertical())
            FrnkText(state = FrnkTextState.Body(text = "Right"))
        }
    }
}
