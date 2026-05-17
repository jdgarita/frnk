package dev.jdgarita.frnk.ui.scaffolds

import dev.jdgarita.frnk.ui.mvi.MviViewModel

/**
 * Thin UI-state machine for [OnboardingScreen]. Owns nothing but the current page index: the host
 * decides what "Completed" and "CloseRequested" mean (mark onboarding done, navigate away, log
 * analytics, …) by collecting effects.
 */
class OnboardingViewModel(
    initial: OnboardingScreenState,
) : MviViewModel<OnboardingScreenState, OnboardingIntent, OnboardingEffect>(initial) {
    override suspend fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.PageSelected ->
                setState { copy(currentPageIndex = intent.index.coerceIn(0, pages.lastIndex)) }
            OnboardingIntent.NextClicked -> {
                if (currentState().isLastPage) {
                    emit(OnboardingEffect.Completed)
                } else {
                    setState { copy(currentPageIndex = currentPageIndex + 1) }
                }
            }
            OnboardingIntent.PreviousClicked ->
                setState { copy(currentPageIndex = (currentPageIndex - 1).coerceAtLeast(0)) }
            OnboardingIntent.CloseClicked -> emit(OnboardingEffect.CloseRequested)
        }
    }
}
