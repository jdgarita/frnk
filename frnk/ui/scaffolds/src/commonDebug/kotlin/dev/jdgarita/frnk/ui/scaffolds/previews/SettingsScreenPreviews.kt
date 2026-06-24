package dev.jdgarita.frnk.ui.scaffolds.previews

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsScreenContent
import dev.jdgarita.frnk.ui.scaffolds.settings.defaultSettingsState
import dev.jdgarita.frnk.ui.theme.Appearance

@Preview
@Composable
private fun SettingsScreen_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        SettingsScreenContent(
            state = defaultSettingsState(version = "v0.1.0", appearance = Appearance.Light),
            onIntent = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun SettingsScreen_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        SettingsScreenContent(
            state = defaultSettingsState(version = "v0.1.0", appearance = Appearance.Dark),
            onIntent = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
private fun SettingsScreen_Pro_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        SettingsScreenContent(
            state =
                defaultSettingsState(
                    version = "v0.1.0",
                    appearance = Appearance.System,
                    isPro = true
                ),
            onIntent = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}