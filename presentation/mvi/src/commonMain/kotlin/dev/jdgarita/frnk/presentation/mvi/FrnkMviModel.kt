package dev.jdgarita.frnk.presentation.mvi

import dev.jdgarita.frnk.domain.framework.AnalyticsEvent
import dev.jdgarita.frnk.domain.framework.AppRatingRequested
import dev.jdgarita.frnk.domain.framework.Outcome
import dev.jdgarita.frnk.domain.framework.error.Error
import dev.jdgarita.frnk.presentation.componentCore.FrnkBottomSheetViewState
import dev.jdgarita.frnk.presentation.componentCore.FrnkDialogViewState
import dev.jdgarita.frnk.presentation.componentCore.ToastAlertViewState
import dev.jdgarita.frnk.util.common.Log
import kotlin.jvm.JvmInline
import kotlin.random.Random
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface FrnkMviModel<
    TArgs : Arguments,
    TIntent : Intent,
    TViewState : ViewStateCommon<TViewState>,
    TExternalEvent : ExternalEvent
    > : MviViewModel<TArgs, TIntent, TViewState, TExternalEvent>, DataLoadingModel

/**
 * A common View Model that layers functionality over a basic MviModel
 *
 * Adds the concepts of
 *  - Data Loading. See [DataLoadingModel]
 *  - Common State. See [CommonState]
 */
abstract class BaseFrnkMviModel<
    TModelState : StateCommon<TModelState>,
    TArgs : Arguments,
    TIntent : Intent,
    TViewState : ViewStateCommon<TViewState>,
    TExternalEvent : ExternalEvent
    >(
    singleThreadDispatcher: CoroutineDispatcher,
    modelStateFactory: ModelStateFactory<TModelState>,
    private val viewModelDependencies: ViewModelDependencies
) : BaseMviModel<TModelState, TArgs, TIntent, TViewState, TExternalEvent>(
    singleThreadDispatcher,
    modelStateFactory
),
    FrnkMviModel<TArgs, TIntent, TViewState, TExternalEvent>,
    DataLoadingModelInternal {

    protected val dataLoadState
        get() = modelState.commonState.dataLoadState

    private val _loadStateFlow: MutableStateFlow<LoadState> = MutableStateFlow(dataLoadState)
    override val loadStateFlow: Flow<LoadState> = _loadStateFlow

    protected val dateFormatter
        get() = viewModelDependencies.formatters.dateTimeFormatter

    protected fun logEvent(event: AnalyticsEvent) = viewModelDependencies.logger.logEvent(event)

    private var screenName: String? = null

    /**
     * Used to indicate  data load should be tracked as a "span" using the data loading interface.
     *
     * Automatically turned on for ScreenViewModels
     *
     * Override and set to null if automatic tracking should not be used.
     */
    open val dataLoadTrackIdentifier: String?
        get() = screenName?.let { "DataLoad:$it" }

    private val dataLoadProperties: MutableMap<String, String> = mutableMapOf()

    /**
     * Indicates if the data should be reloaded when the view is activated.
     * Useful for when one wants to refresh the data when going back to a screen.
     */
    protected open val refreshOnActive: Boolean = false

    override val useDataLoadForTiming: Boolean
        get() = true
    override val useCompositeDataLoadTiming: Boolean
        get() = false

    private var dismissToastJob: Job? = null

    override suspend fun reloadData() {
        if (dataLoadState == LoadState.Initialized) {
            Log.w { "Reloading data before initial load" }
            return
        }
        updateDataLoadState(LoadState.Initialized)
        onDataCleared()
        startDataLoad(isReloading = true)
    }

    override fun onActive() {
        if (refreshOnActive) {
            updateDataLoadState(LoadState.Initialized)
        }

        (this as? ScreenViewModel<*, *, *, *>)?.let {
            screenName = viewModelDependencies.screenNavigationTracker.currentScreen?.name
            viewModelDependencies.logger.breadcrumb("+ $screenName")
        }

        launchAttached {
            when (dataLoadState) {
                LoadState.Failed,
                LoadState.Initialized -> startDataLoad(isReloading = false)

                LoadState.Loaded -> onDataLoaded()
                is LoadState.Loading -> {
                }
            }
        }

        super.onActive()
    }

    override fun onInactive() {
        if (dataLoadState is LoadState.Loading) {
            updateDataLoadState(LoadState.Initialized)
        }

        (this as? ScreenViewModel<*, *, *, *>)?.let {
            viewModelDependencies.logger.breadcrumb("- $screenName")
        }

        modelState.commonState.toastAlertViewState?.let {
            updateCommonState { it.copy(toastAlertViewState = null) }
        }
        dismissToastJob = null

        super.onInactive()
    }

    private fun updateDataFailedToLoadState(error: Any? = null) =
        updateDataLoadState(loadState = LoadState.Failed, error = error as? Error)

    private fun updateDataLoadState(loadState: LoadState, error: Error? = null) {
        updateCommonState { it.copy(dataLoadState = loadState, error = error) }
    }

    private fun updateDataLoadedState() {
        updateCommonState {
            it.copy(
                dataLoadState = LoadState.Loaded,
                hasSuccessfullyLoaded = true,
                error = null
            )
        }
    }

    protected fun getUniqueDataLoadTrackingId(): String = Random.nextInt(0, 1000000).toString()

    protected fun addDataLoadTrackingProperty(key: String, value: String) {
        dataLoadProperties[key] = value
    }

    protected suspend fun showToastAlert(
        toastAlertViewState: ToastAlertViewState,
        dismissDelay: DismissDelay = DismissDelay.Default,
        onToastDismissed: () -> Unit = {}
    ) {
        updateCommonState { it.copy(toastAlertViewState = toastAlertViewState) }
        dismissToastJob?.cancelAndJoin()
        startDelayedToastDismiss(dismissDelay, onToastDismissed)
    }

    protected fun dismissToastAlert() {
        updateCommonState { it.copy(toastAlertViewState = null) }
        dismissToastJob?.cancel()
        dismissToastJob = null
    }

    private fun startDelayedToastDismiss(
        dismissAlertDelayMs: DismissDelay,
        onToastDismissed: () -> Unit
    ) {
        if (dismissAlertDelayMs == DismissDelay.Infinite) {
            return
        }

        val currentJob = dismissToastJob
        if (currentJob == null || currentJob.isActive.not()) {
            dismissToastJob = launchActive {
                if (dismissToastJob != null) {
                    delay(dismissAlertDelayMs.delayMs)
                    updateCommonState { state -> state.copy(toastAlertViewState = null) }
                    dismissToastJob = null
                    onToastDismissed()
                }
            }
        }
    }

    protected fun showDialog(dialogViewState: FrnkDialogViewState) {
        updateCommonState { it.copy(dialogViewState = dialogViewState) }
    }

    protected fun dismissDialog() {
        updateCommonState { it.copy(dialogViewState = null) }
    }

    protected fun showBottomSheet(bottomSheetViewState: FrnkBottomSheetViewState) {
        updateCommonState { it.copy(bottomSheetViewState = bottomSheetViewState) }
    }

    protected fun dismissBottomSheet() {
        updateCommonState { it.copy(bottomSheetViewState = null) }
    }

    protected fun showAppRating() {
        updateCommonState { it.copy(appRatingVisible = true) }
    }

    private fun dismissAppRating() {
        updateCommonState { it.copy(appRatingVisible = false) }
    }

    protected fun startScreenLoadTracking(
        loadDataIdentifier: String,
        suffix: String? = ""
    ) = dataLoadTrackIdentifier?.let {
        viewModelDependencies.logger.startSpan(
            event = it + suffix,
            identifier = loadDataIdentifier,
            properties = dataLoadProperties
        )
    }

    protected fun finishScreenLoadTracking(
        loadDataIdentifier: String,
        loadResult: LoadResult,
        suffix: String? = ""
    ) =
        dataLoadTrackIdentifier?.let {
            viewModelDependencies.logger.endSpan(
                event = it + suffix,
                identifier = loadDataIdentifier,
                properties = dataLoadProperties + mapOf("Result" to loadResult.toString())
            )
        }

    private suspend fun startDataLoad(isReloading: Boolean) {
        val loadDataIdentifer = getUniqueDataLoadTrackingId()
        try {
            if (useDataLoadForTiming) {
                startScreenLoadTracking(loadDataIdentifer)
                updateDataLoadState(LoadState.Loading(isReloading))
            }
            loadData(isReloading)
                .doOnSuccess {
                    updateDataLoadedState()
                    if (useDataLoadForTiming) {
                        finishScreenLoadTracking(loadDataIdentifer, LoadResult.Loaded)
                    }
                    onDataLoaded()
                }.doOnError {
                    updateDataFailedToLoadState(it)
                    if (useDataLoadForTiming) {
                        finishScreenLoadTracking(loadDataIdentifer, LoadResult.Failed)
                    }
                    onDataLoadError()
                }
        } catch (ex: CancellationException) {
            // coroutine was cancelled during load, set it back to initialized
            if (dataLoadState != LoadState.Loaded) {
                updateDataLoadState(LoadState.Initialized)
                if (useDataLoadForTiming) {
                    finishScreenLoadTracking(loadDataIdentifer, LoadResult.Cancelled)
                }
            }
        }
    }

    override suspend fun handleCommonIntent(intent: CommonIntent) {
        when (intent) {
            is CommonIntent.OnRefresh -> reloadData()
            is CommonIntent.OnBackPressed -> handleExternalEvent(CommonExternalEvent.DidPressBack())
            is CommonIntent.OnAppRatingRequested -> {
                viewModelDependencies.logger.logEvent(AppRatingRequested)
                dismissAppRating()
            }
        }
    }

    private fun updateCommonState(block: (CommonState) -> CommonState) =
        updateState { it.copyCommon(block(modelState.commonState)) }

    @JvmInline
    protected value class DismissDelay(val delayMs: Long) {

        companion object {
            val Default = DismissDelay(delayMs = 2000)
            val Infinite = DismissDelay(delayMs = -1)
        }
    }

    protected fun Long.asDismissDelay() = DismissDelay(delayMs = this)
}

/**
 * Indicates the Mvi View Model is data load state aware.
 *
 * It ties data loading into the lifecycle of the view model.  It will attempt
 * to load data when the view is activated.
 *
 * See [LoadState]
 */
interface DataLoadingModel {
    suspend fun reloadData()

    val loadStateFlow: Flow<LoadState>
}

interface DataLoadingModelInternal {

    /**
     * To be implemented by subclass. This method should load the data
     * needed by the view model
     *
     * @param isReloading indicates if the data is being reloaded
     */
    suspend fun loadData(isReloading: Boolean): Outcome<*, *> = Outcome.Success(Unit)

    /**
     * To be implemented by subclass.  This method will be called on data load success
     */
    suspend fun onDataLoaded() = Unit

    /**
     * To be implemented by subclass.  This method will be called on data load error
     */
    suspend fun onDataLoadError() = Unit

    /**
     * To be implemented by subclass.  This method should clear any loaded data
     */
    suspend fun onDataCleared() = Unit

    /**
     * TODO: Ideally this can be true for all view models in the future, and this value can be removed
     */
    val useDataLoadForTiming: Boolean
    val useCompositeDataLoadTiming: Boolean
}