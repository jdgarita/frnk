package dev.jdgarita.frnk.ui.scaffolds.onboarding

import dev.jdgarita.frnk.ui.mvi.ModelMviViewModel
import dev.jdgarita.frnk.ui.scaffolds.onboarding.ext.toUiState

/**
 * Thin UI-state machine for [OnboardingScreen]. Owns nothing but the current page index: the host
 * decides what "Completed" and "CloseRequested" mean (mark onboarding done, navigate away, log
 * analytics, …) by collecting effects.
 *
 * Model-first: the configuration (the page list) arrives as [OnboardingArguments] at attach time
 * (see `onAttached`) rather than via the constructor; the VM mutates [OnboardingModelState] and the engine
 * derives [OnboardingScreenState] through [mapToUiState].
 */
class OnboardingViewModel :
    ModelMviViewModel<OnboardingArguments, OnboardingModelState, OnboardingScreenState, OnboardingIntent, OnboardingEffect>(
        factory = OnboardingModelStateFactory
    ) {
    override fun onAttached(arguments: OnboardingArguments) {
        super.onAttached(arguments)
        updateModel {
            copy(pages = arguments.pages)
        }
    }

    override fun mapToUiState(modelState: OnboardingModelState): OnboardingScreenState =
        OnboardingScreenState(
            pages = modelState.pages.map { it.toUiState() },
            currentPageIndex = modelState.currentPageIndex
        )

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