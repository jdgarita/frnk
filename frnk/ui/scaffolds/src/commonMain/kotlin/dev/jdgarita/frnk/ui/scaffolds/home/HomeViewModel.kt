package dev.jdgarita.frnk.ui.scaffolds.home

import androidx.lifecycle.viewModelScope
import dev.jdgarita.frnk.monetization.usecase.ObserveProStatusUseCase
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarAction
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.iconUpgrade
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * UI-state machine for [FrnkHomeScreen]. Its model holds just the top-bar title plus the reactive
 * `isPro` flag (mirrored in from [ObserveProStatusUseCase]); `mapToUiState` derives the chrome — the
 * top-bar "Upgrade" action shows only while Free. Top-bar interactions re-emit as the matching
 * [HomeEffect] so the host decides what each means (navigate, open the paywall, …) by collecting the
 * effects. Mirrors [dev.jdgarita.frnk.ui.scaffolds.settings.SettingsViewModel].
 */
class HomeViewModel(
    observeProStatus: ObserveProStatusUseCase
) : MviViewModel<HomeArguments, HomeModelState, HomeScreenState, HomeIntent, HomeEffect>(
        factory = HomeModelStateFactory
    ) {
    val isPro: StateFlow<Boolean> = observeProStatus.invoke()

    init {
        // Mirror the reactive Free/Pro status into the model; mapToUiState re-derives the top-bar
        // "Upgrade" action off it (shown only while Free).
        isPro
            .onEach { pro -> updateModel { copy(isPro = pro) } }
            .launchIn(viewModelScope)
    }

    override fun onAttached(arguments: HomeArguments) {
        super.onAttached(arguments)
        updateModel { copy(topBarTitle = arguments.topBarTitle) }
    }

    override fun mapToUiState(modelState: HomeModelState): HomeScreenState =
        HomeScreenState(
            topBar =
                FrnkTopAppBarState(
                    title = modelState.topBarTitle,
                    actions =
                        when {
                            modelState.isPro -> emptyList()
                            else ->
                                listOf(
                                    FrnkTopAppBarAction(
                                        icon = FrnkIconSource.Token(iconUpgrade),
                                        contentDescription = "Upgrade to Pro",
                                        key = "upgrade"
                                    )
                                )
                        }
                )
        )

    override suspend fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.TopBarActionClicked -> emit(HomeEffect.ActionInvoked(intent.action.key))
            HomeIntent.NavigationClicked -> emit(HomeEffect.NavigationInvoked)
            is HomeIntent.ConfigChanged -> updateModel { copy(topBarTitle = intent.topBarTitle) }
        }
    }
}