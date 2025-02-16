package dev.jdgarita.frnk.presentation.mvi

abstract class BaseMviModel : MviViewModel()

/**
 * Methods to be implemented by an implementing MVI View Model
 */
internal interface ModelInternal<
    TModelState : ModelState,
    TArgs : Arguments,
    TIntent : Intent,
    TViewState : ViewState
    > {

    /**
     * Called when a new view is attached
     */
    fun onAttached(args: TArgs)

    /**
     * Called when the view has become active
     */
    fun onActive()

    /**
     * Called when the view has become inactive
     */
    fun onInactive()

    /**
     * Called
     */
    fun onDetached()

    /***
     * Map the current internal Model State to the external View State
     */
    fun mapViewState(currentState: TModelState): TViewState

    /**
     * Handle a new intent given the current state
     */
    suspend fun handleIntent(currentState: TModelState, intent: TIntent)

    /**
     * Handle a new [CommonIntent]
     */
    suspend fun handleCommonIntent(intent: CommonIntent) = Unit
}

/**
 * The internal state of this model, representing all information needed to recreate the state
 */
interface ModelState

interface ModelStateFactory<TModelState : ModelState> {
    fun initialModelState(): TModelState
}