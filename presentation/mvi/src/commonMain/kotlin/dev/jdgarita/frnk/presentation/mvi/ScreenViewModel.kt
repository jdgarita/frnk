package dev.jdgarita.frnk.presentation.mvi

import dev.jdgarita.frnk.presentation.componentCore.FrnkTopBarViewState
import kotlinx.serialization.Serializable

/**
 * Represents a view model for an entire screen
 */
interface ScreenViewModel<
    TArgs : Arguments,
    TIntent : Intent,
    TViewState : ScreenViewState<*>,
    TExternalEvent : ExternalEvent
    > : MviViewModel<TArgs, TIntent, TViewState, TExternalEvent>

/**
 * Represents the state of a screen
 */
interface ScreenViewState<T> : ViewStateCommon<T> {
    val commonScreenViewState: CommonScreenViewState
}

@Serializable
data class CommonScreenViewState(
    val topBarViewState: FrnkTopBarViewState? = null,
    val pullToRefreshEnabled: Boolean = false
)