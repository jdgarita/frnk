package dev.jdgarita.frnk.presentation.identity.splash


import dev.jdgarita.frnk.presentation.mvi.Arguments
import dev.jdgarita.frnk.presentation.mvi.CommonScreenViewState
import dev.jdgarita.frnk.presentation.mvi.CommonViewState
import dev.jdgarita.frnk.presentation.mvi.ExternalEvent
import dev.jdgarita.frnk.presentation.mvi.Intent
import dev.jdgarita.frnk.presentation.mvi.ScreenViewModel
import dev.jdgarita.frnk.presentation.mvi.ScreenViewState

interface SplashViewModel :
    ScreenViewModel<
        SplashArguments,
        SplashIntent,
        SplashViewState,
        SplashExternalEvent
        >

data object SplashArguments : Arguments

sealed class SplashIntent : Intent

sealed class SplashExternalEvent : ExternalEvent {
    data object DidAuthenticate : SplashExternalEvent()
}

data class SplashViewState(
    override val commonViewState: CommonViewState = CommonViewState(),
    override val commonScreenViewState: CommonScreenViewState = CommonScreenViewState()
) : ScreenViewState<SplashViewState>