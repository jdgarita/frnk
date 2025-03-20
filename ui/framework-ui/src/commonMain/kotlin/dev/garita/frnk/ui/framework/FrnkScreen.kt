package dev.garita.frnk.ui.framework

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import dev.garita.frnk.ui.componentLibrary.FrnkTheme
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationRouter
import dev.jdgarita.frnk.presentation.mvi.Arguments
import dev.jdgarita.frnk.presentation.mvi.CommonDisplayError
import dev.jdgarita.frnk.presentation.mvi.CommonIntent
import dev.jdgarita.frnk.presentation.mvi.ExternalEvent
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
 * @param pullToRefreshEnabled Whether pull to refresh is enabled for the screen. Keep in mind
 * that in order for pull to refresh to work, the screen must be scrollable.
 * @param backgroundColor Background color for the screen.
 * @param additionalTopBarContent Additional content for the top bar that will be
 * rendered below top bar from [ScreenViewState].
 * @param content Content for the screen.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <
    TArguments : Arguments,
    TViewState : ScreenViewState<*>,
    TExternalEvent : ExternalEvent
    > FrnkScreen(
    viewModel: MviViewModelWrapper<TArguments, *, TViewState, TExternalEvent>,
    arguments: TArguments,
    navigationRouter: NavigationRouter,
    backgroundColor: Color = FrnkTheme.colors.layoutSurfaceLow,
    additionalTopBarContent: @Composable (ColumnScope.(TViewState) -> Unit)? = null,
    content: @Composable ColumnScope.(TViewState) -> Unit
) {
    val state = viewModel.viewStateFlow.collectAsState(initial = viewModel.viewState).value
    val commonViewState = (state as? ViewStateCommon<*>)?.commonViewState
    val commonScreenViewState = state.commonScreenViewState

    RememberLifecycle(
        viewModel = viewModel,
        navigationRouter = navigationRouter,
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
        ) {
            Column {
                additionalTopBarContent?.let { additionalTopBarContent ->
                    additionalTopBarContent(state)
                }
                content(state)
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
 * @param modifier Modifier for the screen.
 * @param backgroundColor Background color for the screen.
 * @param additionalTopBarContent Additional content for the top bar that will be
 * rendered below top bar from [ScreenViewState].
 * To implement fully custom to bar, leave top bar from [ScreenViewState] null and use this lambda.
 * @param content Content for the screen. The lambda is in the [LazyListScope]
 *  so use [LazyListScope.item] or [LazyListScope.items] to add content.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
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
    backgroundColor: Color = FrnkTheme.colors.layoutSurfaceLow,
    listState: LazyListState = rememberLazyListState(),
    additionalTopBarContent: @Composable (ColumnScope.(TViewState) -> Unit)? = null,
    footerContent: @Composable (BoxScope.(TViewState) -> Unit)? = null,
    content: LazyListScope.(TViewState) -> Unit
) {
    val state = viewModel.viewStateFlow.collectAsState(initial = viewModel.viewState).value
    val commonViewState = (state as? ViewStateCommon<*>)?.commonViewState

    RememberLifecycle(
        viewModel = viewModel,
        arguments = arguments,
        navigationRouter = navigationRouter
    )

    BackHandler {
        viewModel.onIntent(CommonIntent.OnBackPressed)
    }

    Surface(
        modifier = modifier,
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                state = listState
            ) {
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
        }
    }
}

fun <TViewState : ViewState> LazyListScope.errorContent(state: TViewState) =
    if (state is ViewStateCommon<*>) {
        state.commonViewState.commonDisplayError?.let { commonDisplayError ->
            when (commonDisplayError) {
                is CommonDisplayError.EmptyState -> {
//                    item {
//                        SwiftlyEmptyState(state = commonDisplayError.emptyStateViewState)
//                    }
                    false
                }
            }
        } ?: true
    } else {
        true
    }