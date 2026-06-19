package dev.jdgarita.frnk.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle

/**
 * Screen wrapper that attaches a [ModelMviViewModel] to the composition's lifecycle — so screens don't
 * repeat the [RememberMviLifecycle] call themselves — then renders [content].
 *
 * The wrapper is fully generic over the VM's five type params; it doesn't read the state itself (the
 * `content` slot collects it), it only ties [viewModel] and [arguments] together at the type level and
 * forwards them to [RememberMviLifecycle], which calls [ModelMviViewModel.attach] once on first attach.
 */
@Composable
fun <A : Arguments, M : ModelState, S : UiState, I : UiIntent, E : UiEffect> FrnkScreen(
    viewModel: ModelMviViewModel<A, M, S, I, E>,
    arguments: A,
    initialLifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: (uiEffect: UiEffect) -> Unit,
    content: @Composable (state: S) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    val currentOnEffect by rememberUpdatedState(onEffect)

    DisposableEffect(lifecycleOwner, viewModel) {
        viewModel.attach(arguments)
        onDispose { }
    }

    LaunchedEffect(viewModel.effects, lifecycleOwner, initialLifecycleState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(initialLifecycleState) {
            viewModel.effects.collect { currentOnEffect(it) }
        }
    }

    content(state)
}