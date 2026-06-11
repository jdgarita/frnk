package dev.jdgarita.frnk.ui.scaffolds.previews

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.jdgarita.frnk.ui.atoms.FrnkButton
import dev.jdgarita.frnk.ui.atoms.FrnkButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.scaffolds.HomeScreenContent
import dev.jdgarita.frnk.ui.scaffolds.HomeScreenState
import dev.jdgarita.frnk.ui.theme.Appearance

@Preview
@Composable
private fun HomeScreen_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        HomeScreenContent(
            state = HomeScreenState(topBar = FrnkTopAppBarState(title = "Home")),
            onIntent = {},
            modifier = Modifier.fillMaxSize(),
        ) {
            FrnkText(state = FrnkTextState.Title(text = "Welcome back"))
            FrnkText(state = FrnkTextState.Body(text = "Host-provided scrollable content goes here."))
            FrnkButton(state = FrnkButtonState.Content(text = "Get started"), onClick = {})
        }
    }
}

@Preview
@Composable
private fun HomeScreen_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        HomeScreenContent(
            state = HomeScreenState(topBar = FrnkTopAppBarState(title = "Home")),
            onIntent = {},
            modifier = Modifier.fillMaxSize(),
        ) {
            FrnkText(state = FrnkTextState.Title(text = "Welcome back"))
            FrnkText(state = FrnkTextState.Body(text = "Host-provided scrollable content goes here."))
        }
    }
}

@Preview
@Composable
private fun HomeScreen_SkeletonContent() {
    PreviewSurface(appearance = Appearance.Light) {
        HomeScreenContent(
            state = HomeScreenState(topBar = FrnkTopAppBarState(title = "Home")),
            onIntent = {},
            modifier = Modifier.fillMaxSize(),
        ) {
            // The template chrome has no Skeleton state (recorded decision) — loading visuals come
            // from the slot content using the atoms' own sealed Skeleton states.
            FrnkText(state = FrnkTextState.Skeleton)
            FrnkButton(state = FrnkButtonState.Skeleton, onClick = {})
        }
    }
}
