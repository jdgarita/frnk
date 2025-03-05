package dev.jdgarita.frnk.presentation.mvi

import dev.jdgarita.frnk.presentation.componentCore.FrnkSpinnerViewState
import kotlinx.serialization.Serializable

/**
 * CommonViewState is meant to hold data that is common across all views.
 */
@Serializable
data class CommonViewState(
    val dataLoadState: LoadState = LoadState.Initialized,
    val commonDisplayError: CommonDisplayError? = null,
    val spinnerViewState: FrnkSpinnerViewState? = null
) {
    constructor() : this(
        dataLoadState = LoadState.Initialized,
        commonDisplayError = null,
        spinnerViewState = null
    )

    constructor(
        dataLoadState: LoadState,
        commonDisplayError: CommonDisplayError?
    ) : this(
        dataLoadState = dataLoadState,
        commonDisplayError = commonDisplayError,
        spinnerViewState = null
    )
}

interface ViewStateCommon<T> : ViewState {
    val commonViewState: CommonViewState
}