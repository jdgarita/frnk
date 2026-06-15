package dev.jdgarita.frnk.ui.scaffolds.previews

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.scaffolds.FrnkFullScreenScaffold
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingLg

@Composable
private fun FullScreenSample() {
    FrnkFullScreenScaffold(
        onCloseClick = {},
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Theme[spacing][spacingLg]),
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingLg], Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FrnkText(state = FrnkTextState.Title(text = "Full-screen content"))
            FrnkText(
                state =
                    FrnkTextState.Body(
                        text = "Edge-to-edge body drawn under the close button, which sits inside the safe-drawing inset.",
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun FrnkFullScreenScaffold_Light() {
    PreviewSurface(appearance = Appearance.Light) { FullScreenSample() }
}

@Preview
@Composable
private fun FrnkFullScreenScaffold_Dark() {
    PreviewSurface(appearance = Appearance.Dark) { FullScreenSample() }
}
