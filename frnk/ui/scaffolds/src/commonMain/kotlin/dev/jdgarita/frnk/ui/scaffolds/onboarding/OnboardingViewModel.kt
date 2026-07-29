package dev.jdgarita.frnk.ui.scaffolds.onboarding

import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.scaffolds.onboarding.ext.toUiState

/**
 * Thin UI-state machine for [FrnkOnboardingScreen]. Owns nothing but the current page index: the host
 * decides what "Completed" and "CloseRequested" mean (mark onboarding done, navigate away, log
 * analytics, …) by collecting effects.
 *
 * Model-first: the configuration (the page list) arrives as [OnboardingArguments] at attach time
 * (see `onAttached`) rather than via the constructor; the VM mutates [OnboardingModelState] and the engine
 * derives [OnboardingScreenState] through [mapToUiState].
 */
class OnboardingViewModel :
    MviViewModel<OnboardingArguments, OnboardingModelState, OnboardingScreenState, OnboardingIntent, OnboardingEffect>(
        factory = OnboardingModelStateFactory,
        mapper = { modelState ->
            OnboardingScreenState(
                pages = modelState.pages.map { it.toUiState() },
                currentPageIndex = modelState.currentPageIndex
            )
        }
    ) {
    override fun onAttached(arguments: OnboardingArguments) {
        super.onAttached(arguments)
        updateModel {
            copy(pages = arguments.pages)
        }
    }

    override suspend fun onIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.PageSelected ->
                // `lastIndex.coerceAtLeast(0)` guards the pre-attach frame where `pages` is empty
                // (`lastIndex == -1` would make `coerceIn(0, -1)` throw on an empty range).
                updateModel { copy(currentPageIndex = intent.index.coerceIn(0, pages.lastIndex.coerceAtLeast(0))) }

            OnboardingIntent.NextClicked -> {
                val model = currentModel()
                if (model.currentPageIndex == model.pages.lastIndex) {
                    emit(OnboardingEffect.Completed)
                } else {
                    updateModel { copy(currentPageIndex = currentPageIndex + 1) }
                }
            }

            OnboardingIntent.PreviousClicked ->
                updateModel { copy(currentPageIndex = (currentPageIndex - 1).coerceAtLeast(0)) }

            OnboardingIntent.CloseClicked -> emit(OnboardingEffect.CloseRequested)
        }
    }
}