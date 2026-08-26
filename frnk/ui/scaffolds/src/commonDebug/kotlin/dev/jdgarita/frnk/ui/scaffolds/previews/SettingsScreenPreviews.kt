package dev.jdgarita.frnk.ui.scaffolds.previews

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.jdgarita.frnk.ui.scaffolds.settings.FrnkSettingsScreen
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsScreenContent
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsSectionState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsToggleRowState
import dev.jdgarita.frnk.ui.scaffolds.settings.defaultSettingsState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.iconUpgrade

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

// Full-screen previews of FrnkSettingsScreen — the scaffolded entry point (FrnkScreenScaffold top
// bar + content), as a host renders it. The content-only previews above stay for row-level work.

@Preview
@Composable
private fun FrnkSettingsScreen_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkSettingsScreen(
            state = defaultSettingsState(version = "v0.1.0", appearance = Appearance.Light),
            onIntent = {}
        )
    }
}

@Preview
@Composable
private fun FrnkSettingsScreen_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        FrnkSettingsScreen(
            state = defaultSettingsState(version = "v0.1.0", appearance = Appearance.Dark),
            onIntent = {}
        )
    }
}

@Preview
@Composable
private fun FrnkSettingsScreen_Pro_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkSettingsScreen(
            state =
                defaultSettingsState(
                    version = "v0.1.0",
                    appearance = Appearance.System,
                    isPro = true
                ),
            onIntent = {}
        )
    }
}

@Preview
@Composable
private fun FrnkSettingsScreen_DeveloperRevealed_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        FrnkSettingsScreen(
            state =
                defaultSettingsState(version = "v0.1.0", appearance = Appearance.Light)
                    .copy(
                        developerSection =
                            SettingsSectionState(
                                title = FrnkStringSource.Raw("Developer"),
                                rows =
                                    listOf(
                                        SettingsToggleRowState(
                                            id = "god_mode",
                                            icon = FrnkIconSource.Token(iconUpgrade),
                                            title = FrnkStringSource.Raw("God mode"),
                                            subtitle = FrnkStringSource.Raw("Unlock everything on this build"),
                                            checked = true
                                        )
                                    ),
                                footnote = FrnkStringSource.Raw("Revealed by tapping the version 7 times.")
                            ),
                        showDeveloperSection = true
                    ),
            onIntent = {}
        )
    }
}