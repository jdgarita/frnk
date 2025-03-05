package dev.jdgarita.frnk.presentation.mvi

import dev.jdgarita.frnk.domain.framework.error.Error
import dev.jdgarita.frnk.presentation.componentCore.FrnkSpinnerViewState
import dev.jdgarita.frnk.presentation.mvi.errors.asEmptyStateDisplayError

/**
 * CommonState is meant to hold data that is universally common across all
 * Model States.
 *
 * See [CommonViewState]
 *
 * @param hasSuccessfullyLoaded indicates whether the data has successfully loaded
 * at least once. Useful for determining whether to show a skeleton, @see [showLoading] computed property.
 */
data class CommonState(
    val dataLoadState: LoadState = LoadState.Initialized,
    val error: Error? = null,
    val appRatingVisible: Boolean = false,
    val spinnerViewState: FrnkSpinnerViewState? = null,
    val hasSuccessfullyLoaded: Boolean = false
) {

    /**
     * Helper property to determine whether to show a loading skeleton/spinner
     */
    val showLoading: Boolean
        get() = !hasSuccessfullyLoaded || (dataLoadState as? LoadState.Loading)?.isRefreshing == true
}

interface StateCommon<T> : ModelState {
    val commonState: CommonState
    fun copyCommon(commonState: CommonState): T
}

interface CompositeStateCommon<T> : StateCommon<T> {
    val childrenViewState: CommonViewState
    override fun copyCommon(commonState: CommonState): T = copyCommon(commonState, childrenViewState)
    fun copyCommon(childrenViewState: CommonViewState): T = copyCommon(commonState, childrenViewState)
    fun copyCommon(common: CommonState, childrenViewState: CommonViewState): T
}

fun MviViewModel<*, *, *, *>.mapCommonState(
    state: StateCommon<*>
    // stringProvider: FrnkStringProvider
): CommonViewState {
    val displayError: CommonDisplayError? = if (this is ScreenViewModel) {
        when (state) {
            is CompositeStateCommon<*> -> {
                val compositeError = state.commonState.error
                val childrenError = state.childrenViewState.commonDisplayError
                compositeError?.asEmptyStateDisplayError() ?: childrenError
            }

            else -> {
                state.commonState.error?.asEmptyStateDisplayError()
            }
        }
    } else {
        state.commonState.error?.asEmptyStateDisplayError()
    }
    val spinnerViewState: FrnkSpinnerViewState? = when {
        state is CompositeStateCommon<*> ->
            state.commonState.spinnerViewState ?: state.childrenViewState.spinnerViewState
        else -> state.commonState.spinnerViewState
    }

    return CommonViewState(
        dataLoadState = state.commonState.dataLoadState,
        commonDisplayError = displayError,
        spinnerViewState = spinnerViewState
    )
}