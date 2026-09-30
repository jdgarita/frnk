package dev.jdgarita.frnk.ui.app.shell

import dev.jdgarita.frnk.ui.mvi.MviViewModel

/**
 * The presentation state of [FrnkStackShell]'s modal sheets, held here rather than in `remember` so it
 * survives configuration change with the Activity-scoped ViewModel.
 *
 * [FrnkShellIntent.Present] raises a sheet and counts the presentation, so the shell can re-key the
 * sheet's content (and with it its ViewModel store) — including a re-present that lands before the
 * previous presentation's exit slide has finished. A present while the sheet is already up changes
 * nothing, so a double-fired present cannot rebuild the content under the user.
 * [FrnkShellIntent.Dismiss] only drops the key: the count is kept, so the content stays unchanged
 * through its exit.
 */
internal class FrnkShellViewModel :
    MviViewModel<FrnkShellArguments, FrnkShellModelState, FrnkShellState, FrnkShellIntent, FrnkShellEffect>(
        factory = FrnkShellModelStateFactory,
        mapper = ::frnkShellState
    ) {
    override suspend fun onIntent(intent: FrnkShellIntent) {
        when (intent) {
            is FrnkShellIntent.Present ->
                updateModel {
                    if (intent.key in presented) {
                        this
                    } else {
                        val presentation = (presentations[intent.key] ?: 0) + 1
                        copy(
                            presented = presented + intent.key,
                            presentations = presentations + (intent.key to presentation)
                        )
                    }
                }

            is FrnkShellIntent.Dismiss -> updateModel { copy(presented = presented - intent.key) }
        }
    }
}

private fun frnkShellState(modelState: FrnkShellModelState): FrnkShellState =
    FrnkShellState(presented = modelState.presented, presentations = modelState.presentations)