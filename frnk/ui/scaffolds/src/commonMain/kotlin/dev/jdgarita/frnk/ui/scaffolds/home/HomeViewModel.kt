package dev.jdgarita.frnk.ui.scaffolds.home

import dev.jdgarita.frnk.ui.mvi.MviViewModel

/**
 * Thin UI-state machine for [HomeScreen]. Owns nothing but the rendered chrome state: every intent
 * re-emits as the matching [HomeEffect], so the host decides what each interaction means (navigate,
 * open the paywall, create an item, …) by collecting the effects. Mirrors [dev.jdgarita.frnk.ui.scaffolds.settings.SettingsViewModel].
 */
class HomeViewModel(
    initial: HomeScreenState,
) : MviViewModel<HomeScreenState, HomeIntent, HomeEffect>(initial) {
    override suspend fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.TopBarActionClicked -> emit(HomeEffect.ActionInvoked(intent.action.key))
            HomeIntent.NavigationClicked -> emit(HomeEffect.NavigationInvoked)
            is HomeIntent.ConfigChanged -> setState { intent.newState }
        }
    }
}
