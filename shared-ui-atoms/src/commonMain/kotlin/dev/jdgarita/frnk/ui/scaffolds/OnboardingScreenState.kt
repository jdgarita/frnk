package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState

/**
 * One page of the onboarding pager. Reuses atom state types so styling resolves through the same
 * [dev.jdgarita.frnk.ui.theme.FrnkTheme] tokens as the rest of the toolkit.
 */
@Immutable
data class OnboardingPageState(
    val title: FrnkTextState,
    val description: FrnkTextState? = null,
    val icon: FrnkIconState? = null,
)

/**
 * Configuration + runtime state for [OnboardingScreen]. The `pages` list is treated as immutable
 * for the lifetime of the screen — pass the full list at construction time rather than mutating
 * it through intents.
 *
 * **Invariant:** [pages] must be non-empty. An onboarding flow with no pages has no meaningful UI
 * to render (the pager would be empty, the pip row would be empty, and the Next button would have
 * nothing to advance to) — the constructor rejects the call rather than silently rendering a broken
 * screen. Because state mutations go through `copy(...)`, which re-runs `init`, the invariant
 * holds for the lifetime of every [OnboardingScreenState] instance.
 *
 * [pagerHeight] is the single "configurable size" knob: `null` (default) lets the pager fill the
 * remaining vertical space via `Modifier.weight(1f)`; non-null pins the pager to that exact height,
 * useful when the host wants the buttons to sit above the keyboard or below a hero region.
 */
@Immutable
data class OnboardingScreenState(
    val pages: List<OnboardingPageState>,
    val currentPageIndex: Int = 0,
    val pagerHeight: Dp? = null,
    val userScrollEnabled: Boolean = true,
) : UiState {
    init {
        require(pages.isNotEmpty()) { "OnboardingScreenState requires at least one page." }
    }

    val isFirstPage: Boolean get() = currentPageIndex == 0
    val isLastPage: Boolean get() = currentPageIndex == pages.lastIndex
}

sealed interface OnboardingIntent : UiIntent {
    data class PageSelected(
        val index: Int,
    ) : OnboardingIntent

    data object NextClicked : OnboardingIntent

    data object PreviousClicked : OnboardingIntent

    data object CloseClicked : OnboardingIntent
}

sealed interface OnboardingEffect : UiEffect {
    data object CloseRequested : OnboardingEffect

    data object Completed : OnboardingEffect
}
