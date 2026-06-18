package dev.jdgarita.frnk.ui.scaffolds.onboarding

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.mvi.Arguments
import dev.jdgarita.frnk.ui.mvi.ModelState
import dev.jdgarita.frnk.ui.mvi.ModelStateFactory
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
    val icon: FrnkIconState? = null
)

/**
 * The data-only runtime input for [OnboardingViewModel], supplied at **attach time** (see
 * [dev.jdgarita.frnk.ui.mvi.RememberMviLifecycle] / [dev.jdgarita.frnk.ui.mvi.FrnkScreen]) rather than
 * through the constructor. Carries the onboarding configuration — the immutable page list — which
 * `onAttached` seeds into [OnboardingModelState].
 *
 * **Invariant:** [pages] must be non-empty. An onboarding flow with no pages has no meaningful UI to
 * render (an empty pager, an empty pip row, a Next button with nothing to advance to) — this is the
 * input that must be valid, so the guard lives here.
 */
@Immutable
data class OnboardingArguments(
    val pages: List<OnboardingPageState>
) : Arguments {
    init {
        require(pages.isNotEmpty()) { "OnboardingArguments requires at least one page." }
    }
}

/**
 * The data-only layer for [OnboardingViewModel]. Seeded empty by [OnboardingModelStateFactory] at
 * construction and filled from [OnboardingArguments] in `onAttached`; the VM mutates [currentPageIndex]
 * as the user navigates.
 */
@Immutable
data class OnboardingModelState(
    val pages: List<OnboardingPageState> = emptyList(),
    val currentPageIndex: Int = 0
) : ModelState

object OnboardingModelStateFactory : ModelStateFactory<OnboardingModelState> {
    override fun initialModelState() = OnboardingModelState()
}

/**
 * The rendered state [OnboardingScreen] collects — derived from [OnboardingModelState] via the VM's
 * `mapToUiState`. Tolerates an empty [pages] list: the engine maps the empty initial model into a
 * UiState once at VM construction, before `onAttached` seeds the pages. The non-empty guarantee lives on
 * [OnboardingArguments] (the input), not here.
 */
@Immutable
data class OnboardingScreenState(
    val pages: List<OnboardingPageState>,
    val currentPageIndex: Int = 0
) : UiState {
    val isFirstPage: Boolean get() = currentPageIndex == 0
    val isLastPage: Boolean get() = currentPageIndex == pages.lastIndex
}

sealed interface OnboardingIntent : UiIntent {
    data class PageSelected(
        val index: Int
    ) : OnboardingIntent

    data object NextClicked : OnboardingIntent

    data object PreviousClicked : OnboardingIntent

    data object CloseClicked : OnboardingIntent
}

sealed interface OnboardingEffect : UiEffect {
    data object CloseRequested : OnboardingEffect

    data object Completed : OnboardingEffect
}