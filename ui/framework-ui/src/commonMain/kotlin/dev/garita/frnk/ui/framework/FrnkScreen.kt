package dev.garita.frnk.ui.framework

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import dev.garita.frnk.ui.componentLibrary.FrnkTheme
import dev.jdgarita.frnk.presentation.componentCore.InContentAlertViewState
import dev.jdgarita.frnk.presentation.componentCore.ToastAlertViewState
import dev.jdgarita.frnk.presentation.mvi.Arguments
import dev.jdgarita.frnk.presentation.mvi.CommonDisplayError
import dev.jdgarita.frnk.presentation.mvi.CommonIntent
import dev.jdgarita.frnk.presentation.mvi.ExternalEvent
import dev.jdgarita.frnk.presentation.mvi.LoadState
import dev.jdgarita.frnk.presentation.mvi.MviViewModelWrapper
import dev.jdgarita.frnk.presentation.mvi.ScreenViewState
import dev.jdgarita.frnk.presentation.mvi.ViewState
import dev.jdgarita.frnk.presentation.mvi.ViewStateCommon

/**
 * Composable for displaying a screen.
 * This component manages the wiring of lifecycle and navigation events. Additionally,
 * it handles the back button functionality. Note - it does not display error content.
 *
 * @param viewModel ViewModel for the screen.
 * @param arguments Arguments for the screen.
 * @param navigationRouter Navigation router for the screen.
 * @param pullToRefreshEnabled Whether pull to refresh is enabled for the screen. Keep in mind
 * that in order for pull to refresh to work, the screen must be scrollable.
 * @param backgroundColor Background color for the screen.
 * @param additionalTopBarContent Additional content for the top bar that will be
 * rendered below top bar from [ScreenViewState].
 * @param bottomSheetContent Content for the bottom sheet. The lambda receives the bottom sheet id.
 * @param content Content for the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <
    TArguments : Arguments,
    TViewState : ScreenViewState<*>,
    TExternalEvent : ExternalEvent
    > FrnkScreen(
    viewModel: MviViewModelWrapper<TArguments, *, TViewState, TExternalEvent>,
    arguments: TArguments,
    navigationRouter: NavigationRouter,
    pullToRefreshEnabled: Boolean = false,
    backgroundColor: Color = FrnkTheme.colors.layoutSurfaceLow,
    additionalTopBarContent: @Composable (ColumnScope.(TViewState) -> Unit)? = null,
    bottomSheetContent: @Composable ((bottomSheetId: String) -> Unit)? = null,
    content: @Composable ColumnScope.(TViewState) -> Unit
) {
    val state = viewModel.viewStateFlow.collectAsState(initial = viewModel.viewState).value
    val commonViewState = (state as? ViewStateCommon<*>)?.commonViewState
    val commonScreenViewState = state.commonScreenViewState
    val isRefreshing = (commonViewState?.dataLoadState as? LoadState.Loading)?.isRefreshing ?: false
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.onIntent(CommonIntent.OnRefresh) }
    )
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    RememberLifecycle(
        navigationRouter = navigationRouter,
        viewModel = viewModel,
        arguments = arguments
    )

    BackHandler {
        viewModel.onIntent(CommonIntent.OnBackPressed)
    }

    Surface(
        Modifier.fillMaxSize(),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .pullRefresh(
                    state = pullRefreshState,
                    enabled = commonScreenViewState.pullToRefreshEnabled || pullToRefreshEnabled
                )
        ) {
            Column {
                commonScreenViewState.topBarViewState?.let { FrnkTopBar(state = it) }
                additionalTopBarContent?.let { additionalTopBarContent ->
                    additionalTopBarContent(state)
                }
                content(state)
            }

            PullToRefreshIndicator(
                isRefreshing = isRefreshing,
                pullRefreshState = pullRefreshState
            )

            commonViewState?.spinnerViewState?.let {
                FrnkSpinner(viewState = it)
            }
        }
    }
}

/**
 * Composable for displaying a screen that uses a [LazyColumn] as its root view.
 *
 * This component manages the wiring of lifecycle and navigation events. Additionally,
 * it handles the back button and pull to refresh functionalities, and displays error content sourced from the
 * [CommonViewState.commonDisplayError] mechanism.
 *
 * @param viewModel ViewModel for the screen.
 * @param arguments Arguments for the screen.
 * @param navigationRouter Navigation router for the screen.
 * @param modifier Modifier for the screen.
 * @param backgroundColor Background color for the screen.
 * @param additionalTopBarContent Additional content for the top bar that will be
 * rendered below top bar from [ScreenViewState].
 * To implement fully custom to bar, leave top bar from [ScreenViewState] null and use this lambda.
 * @param content Content for the screen. The lambda is in the [LazyListScope]
 * @param bottomSheetContent Content for the bottom sheet. The lambda receives the bottom sheet id.
 *  so use [LazyListScope.item] or [LazyListScope.items] to add content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <
    TArguments : Arguments,
    TViewState : ScreenViewState<*>,
    TExternalEvent : ExternalEvent
    > FrnkListScreen(
    viewModel: MviViewModelWrapper<TArguments, *, TViewState, TExternalEvent>,
    arguments: TArguments,
    navigationRouter: NavigationRouter,
    modifier: Modifier = Modifier,
    backgroundColor: Color = SwiftlyTheme.colors.layoutSurfaceLow,
    listState: LazyListState = rememberLazyListState(),
    visibilityThreshold: Float = 0.5F,
    additionalTopBarContent: @Composable (ColumnScope.(TViewState) -> Unit)? = null,
    footerContent: @Composable (BoxScope.(TViewState) -> Unit)? = null,
    bottomSheetContent: @Composable ((bottomSheetId: String) -> Unit)? = null,
    content: LazyListScope.(TViewState) -> Unit
) {
    val state = viewModel.viewStateFlow.collectAsState(initial = viewModel.viewState).value
    val commonViewState = (state as? ViewStateCommon<*>)?.commonViewState
    val isRefreshing = (commonViewState?.dataLoadState as? LoadState.Loading)?.isRefreshing ?: false
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.onIntent(CommonIntent.OnRefresh) }
    )
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    RememberLifecycle(
        navigationRouter = navigationRouter,
        viewModel = viewModel,
        arguments = arguments
    )

    BackHandler {
        viewModel.onIntent(CommonIntent.OnBackPressed)
    }

    Surface(
        modifier = modifier,
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.pullRefresh(
                state = pullRefreshState,
                enabled = state.commonScreenViewState.pullToRefreshEnabled
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(TestTag.SwiftlyScreenLazyColumn),
                state = listState
            ) {
                topBar(state, additionalTopBarContent)
                val loadContent = errorContent(state)
                if (loadContent) {
                    content(state)
                }
            }

            footerContent?.let { footer ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .wrapContentSize()
                ) {
                    footer(state)
                }
            }

            PullToRefreshIndicator(
                isRefreshing = isRefreshing,
                pullRefreshState = pullRefreshState
            )



            commonViewState?.spinnerViewState?.let {
                SwiftlySpinner(viewState = it)
            }
        }
    }
}

private fun <TViewState : ScreenViewState<*>> LazyListScope.topBar(
    viewState: TViewState,
    additionalTopBarContent: @Composable (ColumnScope.(TViewState) -> Unit)? = null
) {
    val topBarViewState = viewState.commonScreenViewState.topBarViewState
    if (topBarViewState != null || additionalTopBarContent != null) {
        swiftlyStickyHeader {
            Column(modifier = Modifier.background(SwiftlyTheme.colors.topNavBarTopNavBarSurface)) {
                topBarViewState?.let { SwiftlyTopBar(state = it) }
                additionalTopBarContent?.let { it(viewState) }
            }
        }
    }
}

@Composable
private fun BoxScope.PullToRefreshIndicator(
    isRefreshing: Boolean,
    pullRefreshState: PullRefreshState
) {
    PullRefreshIndicator(
        modifier = Modifier.align(Alignment.TopCenter),
        backgroundColor = SwiftlyTheme.colors.layoutSurfaceLow,
        contentColor = SwiftlyTheme.colors.accentsPrimary,
        refreshing = isRefreshing,
        scale = true,
        state = pullRefreshState
    )
}

fun <TViewState : ViewState> LazyListScope.errorContent(state: TViewState) =
    if (state is ViewStateCommon<*>) {
        state.commonViewState.commonDisplayError?.let { commonDisplayError ->
            when (commonDisplayError) {
                is CommonDisplayError.Alert -> {
                    errorAlertContent(commonDisplayError)
                    true
                }

                is CommonDisplayError.EmptyState -> {
                    item {
                        SwiftlyEmptyState(state = commonDisplayError.emptyStateViewState)
                    }
                    false
                }
            }
        } ?: true
    } else {
        true
    }

private fun LazyListScope.errorAlertContent(commonDisplayError: CommonDisplayError.Alert) {
    when (val alertViewState = commonDisplayError.alertViewState) {
        is InContentAlertViewState -> item {
            InContentAlert(state = alertViewState)
        }

        is ToastAlertViewState -> item {
            ToastAlert(state = alertViewState)
        }
    }
}