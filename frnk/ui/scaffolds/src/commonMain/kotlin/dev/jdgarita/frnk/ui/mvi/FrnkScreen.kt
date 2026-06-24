package dev.jdgarita.frnk.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle

/**
 * Screen wrapper that attaches a [MviViewModel] to the composition's lifecycle (calling
 * [MviViewModel.attach] once on first attach), collects its state lifecycle-aware, consumes its effect
 * channel, and renders [content] with the collected state.
 *
 * By default it also installs a `BackHandler` that routes system back to
 * [CommonUiIntent.OnBackPressed]. Screens that need their own back semantics (e.g. a tabbed scaffold whose
 * back convention depends on the active tab + its stack depth) pass [handleBackPressed] `= false` and place
 * their own `BackHandler` inside [content], where the collected `state` is in scope.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <A : Arguments, M : ModelState, S : UiState, I : UiIntent, E : UiEffect> FrnkScreen(
    viewModel: MviViewModel<A, M, S, I, E>,
    arguments: A,
    initialLifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    handleBackPressed: Boolean = true,
    onEffect: (uiEffect: UiEffect) -> Unit,
    content: @Composable (state: S) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    val currentOnEffect by rememberUpdatedState(onEffect)

    if (handleBackPressed) {
        BackHandler(enabled = true) {
            viewModel.onIntent(CommonUiIntent.OnBackPressed)
        }
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        viewModel.attach(arguments)
        onDispose { }
    }

    LaunchedEffect(viewModel.effects, lifecycleOwner, initialLifecycleState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(initialLifecycleState) {
            viewModel.effects.collect { newUiEffect ->
                currentOnEffect(newUiEffect)
            }
        }
    }

    content(state)
}