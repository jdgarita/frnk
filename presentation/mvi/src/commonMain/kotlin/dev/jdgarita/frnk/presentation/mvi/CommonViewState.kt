package dev.jdgarita.frnk.presentation.mvi

import dev.jdgarita.frnk.presentation.componentCore.FrnkBottomSheetViewState
import dev.jdgarita.frnk.presentation.componentCore.FrnkDialogViewState
import dev.jdgarita.frnk.presentation.componentCore.FrnkSpinnerViewState
import dev.jdgarita.frnk.presentation.componentCore.ToastAlertViewState
import dev.jdgarita.frnk.presentation.componentCore.ViewState
import kotlinx.serialization.Serializable

/**
 * CommonViewState is meant to hold data that is common across all views.
 */
@Serializable
data class CommonViewState(
    val dataLoadState: LoadState = LoadState.Initialized,
    val commonDisplayError: CommonDisplayError? = null,
    val toastAlertViewState: ToastAlertViewState? = null,
    val dialogViewState: FrnkDialogViewState? = null,
    val bottomSheetViewState: FrnkBottomSheetViewState? = null,
    val appRatingVisible: Boolean = false,
    val spinnerViewState: FrnkSpinnerViewState? = null
) {
    constructor() : this(
        dataLoadState = LoadState.Initialized,
        commonDisplayError = null,
        toastAlertViewState = null,
        dialogViewState = null,
        bottomSheetViewState = null,
        appRatingVisible = false,
        spinnerViewState = null
    )

    constructor(
        dataLoadState: LoadState,
        commonDisplayError: CommonDisplayError?
    ) : this(
        dataLoadState = dataLoadState,
        commonDisplayError = commonDisplayError,
        toastAlertViewState = null,
        dialogViewState = null,
        bottomSheetViewState = null,
        appRatingVisible = false,
        spinnerViewState = null
    )
}

interface ViewStateCommon<T> : ViewState {
    val commonViewState: CommonViewState
}