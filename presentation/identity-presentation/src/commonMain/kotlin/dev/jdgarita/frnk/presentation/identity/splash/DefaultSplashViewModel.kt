package dev.jdgarita.frnk.presentation.identity.splash

import dev.jdgarita.frnk.domain.framework.outcome.Outcome
import dev.jdgarita.frnk.domain.framework.outcome.toOutcomeSuccess
import dev.jdgarita.frnk.presentation.mvi.BaseFrnkMviModel
import dev.jdgarita.frnk.presentation.mvi.CommonIntent
import dev.jdgarita.frnk.presentation.mvi.CommonState
import dev.jdgarita.frnk.presentation.mvi.ModelStateFactory
import dev.jdgarita.frnk.presentation.mvi.StateCommon
import dev.jdgarita.frnk.presentation.mvi.ViewModelDependencies
import dev.jdgarita.frnk.presentation.mvi.mapCommonState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

class DefaultSplashViewModel(
    viewModelDependencies: ViewModelDependencies
) : SplashViewModel, BaseFrnkMviModel<
    DefaultSplashViewModel.SplashModelState,
    SplashArguments,
    SplashIntent,
    SplashViewState,
    SplashExternalEvent
    >(
    viewModelDependencies = viewModelDependencies,
    modelStateFactory = SplashModelStateFactory
) {

    private val authFinishedFlow: MutableSharedFlow<Unit> = MutableSharedFlow()

    override fun onActive() {
        super.onActive()

        launchActive {
            delay(5000)
            handleExternalEvent(SplashExternalEvent.DidAuthenticate)
        }
    }

    override fun mapViewState(currentState: SplashModelState): SplashViewState = SplashViewState(
        commonViewState = mapCommonState(currentState)
        // todo map logoViewState
    )

    override suspend fun handleIntent(
        currentState: SplashModelState,
        intent: SplashIntent
    ) = Unit

    override suspend fun handleCommonIntent(intent: CommonIntent) {
        super.handleCommonIntent(intent)
    }

    override suspend fun loadData(isReloading: Boolean): Outcome<*, *> =
        authFinishedFlow.first().toOutcomeSuccess()

    data class SplashModelState(
        override val commonState: CommonState = CommonState()
    ) : StateCommon<SplashModelState> {
        override fun copyCommon(commonState: CommonState): SplashModelState = copy(commonState = commonState)
    }

    object SplashModelStateFactory : ModelStateFactory<SplashModelState> {
        override fun initialModelState(): SplashModelState = SplashModelState()
    }
}