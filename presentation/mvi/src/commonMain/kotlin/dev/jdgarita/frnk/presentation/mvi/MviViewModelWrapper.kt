package dev.jdgarita.frnk.presentation.mvi

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow

/**
 * Wrapper for the shared [MviViewModel] that extends the Android [ViewModel] to take advantage of
 * that functionality.
 */
open class MviViewModelWrapper<
    TArgs : Arguments,
    TIntent : Intent,
    TViewState : ViewState,
    TExternalEvent : ExternalEvent
    >(
    val model: MviViewModel<TArgs, TIntent, TViewState, TExternalEvent>
) : ViewModel(), MviViewModel<TArgs, TIntent, TViewState, TExternalEvent> {

    override val viewState: TViewState = model.viewState

    override val viewStateFlow: Flow<TViewState> = model.viewStateFlow

    override val externalEventFlow: Flow<ExternalEvent> = model.externalEventFlow

    override fun cancelCallbackSubscriptions() = model.cancelCallbackSubscriptions()

    override val lifecycleState: LifecyleState = model.lifecycleState

    override fun activate() = model.activate()

    override fun deactivate() = model.deactivate()

    override fun detachView() = model.detachView()

    override fun attachView(args: TArgs) = model.attachView(args)

    override fun subscribeExternalEvents(onEvent: (ExternalEvent) -> Unit) = model.subscribeExternalEvents(onEvent)

    override fun onIntent(intent: TIntent) = model.onIntent(intent)

    override fun onIntent(intent: CommonIntent) = model.onIntent(intent)

    override val active: Boolean
        get() = model.active

    @CallSuper
    override fun onCleared() {
        model.detachView()

        super.onCleared()
    }
}