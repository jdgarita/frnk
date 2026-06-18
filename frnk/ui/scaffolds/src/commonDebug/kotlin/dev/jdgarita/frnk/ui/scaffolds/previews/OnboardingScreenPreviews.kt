package dev.jdgarita.frnk.ui.scaffolds.previews

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingPageState
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingScreenContent
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingScreenState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.iconCheck
import dev.jdgarita.frnk.ui.theme.iconSearch
import dev.jdgarita.frnk.ui.theme.iconSettings
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize

@Composable
private fun samplePages(): List<OnboardingPageState> {
    // Mirror the remember(...) pattern used in DemoScreen.demoOnboardingState(): icon tokens are
    // stable across recompositions unless the host swaps iconOverrides, so the 3 page allocations
    // happen once instead of on every preview recomposition.
    val checkIcon = Theme[icons][iconCheck]
    val searchIcon = Theme[icons][iconSearch]
    val settingsIcon = Theme[icons][iconSettings]
    return remember(checkIcon, searchIcon, settingsIcon) {
        val pageIcons = listOf(checkIcon, searchIcon, settingsIcon)
        val titles =
            listOf(
                "Welcome to Frnk" to "A Kotlin Multiplatform toolkit for building polished, opinionated apps in days, not weeks.",
                "Search everything" to "One unified surface across your data sources — typed, paginated, and offline-ready.",
                "Ready when you are" to "Tap Get Started to begin your first session.",
            )
        titles.mapIndexed { i, (title, body) ->
            OnboardingPageState(
                title = FrnkTextState.Title(text = title),
                description = FrnkTextState.Body(text = body),
                icon =
                    FrnkIconState.Content(
                        imageVector = pageIcons[i],
                        contentDescription = null,
                        size = FrnkIconSize.xxl,
                        tint = colorPrimary,
                    ),
            )
        }
    }
}

@Composable
private fun sampleState(pageIndex: Int): OnboardingScreenState {
    val pages = samplePages()
    return remember(pages, pageIndex) {
        OnboardingScreenState(pages = pages, currentPageIndex = pageIndex)
    }
}

@Preview
@Composable
private fun OnboardingScreen_FirstPage_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        OnboardingScreenContent(state = sampleState(0), onIntent = {}, modifier = Modifier.fillMaxSize())
    }
}

@Preview
@Composable
private fun OnboardingScreen_MiddlePage_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        OnboardingScreenContent(state = sampleState(1), onIntent = {}, modifier = Modifier.fillMaxSize())
    }
}

@Preview
@Composable
private fun OnboardingScreen_LastPage_Light() {
    PreviewSurface(appearance = Appearance.Light) {
        OnboardingScreenContent(state = sampleState(2), onIntent = {}, modifier = Modifier.fillMaxSize())
    }
}

@Preview
@Composable
private fun OnboardingScreen_FirstPage_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        OnboardingScreenContent(state = sampleState(0), onIntent = {}, modifier = Modifier.fillMaxSize())
    }
}

@Preview
@Composable
private fun OnboardingScreen_MiddlePage_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        OnboardingScreenContent(state = sampleState(1), onIntent = {}, modifier = Modifier.fillMaxSize())
    }
}

@Preview
@Composable
private fun OnboardingScreen_LastPage_Dark() {
    PreviewSurface(appearance = Appearance.Dark) {
        OnboardingScreenContent(state = sampleState(2), onIntent = {}, modifier = Modifier.fillMaxSize())
    }
}
