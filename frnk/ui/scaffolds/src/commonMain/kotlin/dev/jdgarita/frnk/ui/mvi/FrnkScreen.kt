package dev.jdgarita.frnk.ui.mvi

import androidx.compose.runtime.Composable

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
    content: @Composable () -> Unit,
) {
    RememberMviLifecycle(viewModel = viewModel, arguments = arguments)
    content()
}
