@file:OptIn(ExperimentalUuidApi::class)

package dev.jdgarita.frnk.presentation.framework.navigation

import dev.jdgarita.frnk.domain.framework.ScreenName
import dev.jdgarita.frnk.domain.framework.ScreenNavigationTracker
import dev.jdgarita.frnk.presentation.mvi.CommonExternalEvent
import dev.jdgarita.frnk.presentation.mvi.ExternalEvent
import dev.jdgarita.frnk.util.common.Log
import dev.jdgarita.frnk.util.common.ext.truncate
import kotlin.reflect.KClass
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * @param navigationResult - we now handle the [UserActionEvent.BackPress] event in the base class so we don't need to
 * remember that we need to implement this in every new nav router to account for iOS back events.
 * the [navigationResult] is passed as the end event for that
 */
abstract class BaseNavigationRouter<TResult : NavigationRouterResult>(
    coroutineDispatcher: CoroutineDispatcher,
    private val navigationContextSwitcher: NavigationContextSwitcher,
    private val screenNavigationTracker: ScreenNavigationTracker,
    private val navigationResult: TResult
) : NavigationRouter {

    abstract fun processStartEvent(externalEvent: ExternalEvent)
    abstract fun processExternalEvent(externalEvent: ExternalEvent)
    abstract fun processChildNavigationResult(result: NavigationRouterResult)

    private val navigationHandlers: MutableMap<KClass<out ExternalEvent>, () -> NavigationRouter> = mutableMapOf()

    private var parentNavigationRouter: NavigationRouter? = null

    private var childNavigationRouter: NavigationRouter? = null

    internal val _navigationScreenStack: MutableList<ScreenEntry> = mutableListOf()
    override val navigationScreenStack: List<ScreenEntry>
        get() = _navigationScreenStack.toList()

    override val fullNavigationStackSize: Int
        get() = navigationScreenStack.size + (childNavigationRouter?.fullNavigationStackSize ?: 0)

    override val activeNavigationRouter: NavigationRouter
        get() = childNavigationRouter?.activeNavigationRouter ?: this

    protected val scope = CoroutineScope(coroutineDispatcher + SupervisorJob())

    private val _eventFlow = MutableSharedFlow<NavigationEvent>()
    override val eventFlow: SharedFlow<NavigationEvent> = _eventFlow.asSharedFlow()

    override var isStarted: Boolean = false
    override var shouldReset: Boolean = false

    private var finishOnEmptyStack: Boolean = true
    private var changingContext: Boolean = false

    private val mutex = Mutex()
    private var navigationContext: NavigationContext? = null

    final override fun handleExternalEvent(externalEvent: ExternalEvent) {
        // If there's already a child, let that child handle it
        val navigationHandler = navigationHandlers[externalEvent::class]
        when {
            childNavigationRouter != null -> childNavigationRouter?.handleExternalEvent(externalEvent)
            navigationHandler != null -> {
                // If there's a registered handler for this type of event, start the navigation router
                childNavigationRouter = navigationHandler.invoke()
                scope.launch {
                    childNavigationRouter?.eventFlow?.collect { _eventFlow.emit(it) }
                }
                childNavigationRouter?.start(externalEvent, this)
            }

            externalEvent is CommonExternalEvent.DidPressBack -> externalEvent.screenId?.let {
                popIfShowing(it, navigationResult)
            } ?: popLastScreen(navigationResult)

            else -> processExternalEvent(externalEvent)
        }
    }

    /**
     * Debug method only
     */
    protected fun printChildRouters() {
        Log.i("Current nav router stack: ${printChildRoutersRecursive(this)}")
    }

    private fun printChildRoutersRecursive(baseRouter: BaseNavigationRouter<*>): String =
        baseRouter.childNavigationRouter?.let {
            "$baseRouter + ${printChildRoutersRecursive(it as BaseNavigationRouter<*>)}"
        } ?: baseRouter.toString()

    override fun start(externalEvent: ExternalEvent, navigationContext: NavigationContext) {
        isStarted = true
        this.navigationContext = navigationContext
        activeNavigationRouter.reset()
        processStartEvent(externalEvent)
    }

    override fun start(externalEvent: ExternalEvent, parentNavigationRouter: NavigationRouter) {
        this.parentNavigationRouter = parentNavigationRouter
        processStartEvent(externalEvent)
    }

    override fun onChildNavigationRouterFinished(result: NavigationRouterResult) {
        childNavigationRouter = null
        processChildNavigationResult(result)
    }

    override fun <T : ExternalEvent> registerNavigationHandler(
        externalEvent: KClass<out T>,
        navigationRouterProvider: () -> NavigationRouter
    ) {
        navigationHandlers[externalEvent] = navigationRouterProvider
    }

    /**
     * Called when a new screen should be shown
     */
    protected fun pushScreen(
        screen: NavigationScreen,
        navigationType: NavigationType,
        screenAttributes: Map<String, String> = emptyMap(),
        ignoreIfMatchesLast: Boolean = false
    ): String {
        if (ignoreIfMatchesLast && navigationScreenStack.lastOrNull().matchesName(screen)) {
            return navigationScreenStack.last().id
        }
        val screenEntry = ScreenEntry(
            "${Uuid.random()}",
            screen
        )
        screenNavigationTracker.onScreenChanged(screen.screenName, screenAttributes)

        scope.launch {
            mutex.withLock {
                _navigationScreenStack.add(screenEntry)
                _eventFlow.emit(
                    NavigationEvent(
                        screenEntry,
                        navigationType
                    )
                )
            }
        }
        return screenEntry.id
    }

    protected fun changeContext(externalEvent: ExternalEvent, finishOtherContexts: Boolean = false) {
        scope.launch {
            mutex.withLock {
                navigationContextSwitcher.switchContext(externalEvent, finishOtherContexts)
            }
        }
    }

    protected fun handleExternalUrl(
        url: String,
        name: ScreenName = ScreenName.ExternalUrlEvent
    ) {
        val screenId = pushScreen(
            screen = ExternalUrlScreen(url, name),
            navigationType = NavigationType.FULL_SCREEN
        )
        popIfShowing(screenId, navigationResult)
    }

    protected fun handleEmailComposer(email: String, name: ScreenName) {
        val screenId = pushScreen(
            screen = EmailComposerScreen(email, name),
            navigationType = NavigationType.FULL_SCREEN
        )
        popIfShowing(screenId, navigationResult)
    }

    protected fun popLastScreen(result: TResult, finishIfEmptyStack: Boolean = true) {
        popIfShowing(navigationScreenStack.lastOrNull()?.id, result, finishIfEmptyStack)
    }

    /**
     * Called when the topmost screen should be finished with a result
     *
     * Pops the screen only if the screenId matches
     */
    protected fun popIfShowing(screenId: String?, result: TResult, finishIfEmptyStack: Boolean = true) {
        scope.launch {
            mutex.withLock {
                finishOnEmptyStack = finishIfEmptyStack
                val lastStackEntry = _navigationScreenStack.lastOrNull()
                if (lastStackEntry != null) {
                    if (lastStackEntry.id == screenId) {
                        _navigationScreenStack.removeAt(_navigationScreenStack.lastIndex)
                        _eventFlow.emit(
                            NavigationEvent(lastStackEntry, NavigationType.DISMISS)
                        )
                    } else {
                        // If the ID doesn't match the last on the stack, attempt to find the entry in the stack
                        val index = _navigationScreenStack.indexOfLast { it.id == screenId }
                        if (index > -1) {
                            val removed = _navigationScreenStack.removeAt(index)
                            _navigationScreenStack.truncate(index)
                            _eventFlow.emit(
                                NavigationEvent(removed, NavigationType.CLEAR_STACK)
                            )
                        }
                    }
                }
                if (_navigationScreenStack.isEmpty() && finishIfEmptyStack) {
                    onFinishNavigation(result)
                } else {
                    _navigationScreenStack.lastOrNull()?.screen?.screenName?.let {
                        screenNavigationTracker.onScreenChanged(
                            it
                        )
                    }
                }
            }
        }
    }

    /**
     * Called when this navigation router is finished
     */
    protected fun popAll(result: TResult) {
        scope.launch {
            mutex.withLock {
                internalPopAll(result)
            }
        }
    }

    /**
     * Called when all screens should be finished and the context should be changed
     */
    protected fun popAllAndChangeContext(
        result: TResult,
        externalEvent: ExternalEvent,
        finishOtherContexts: Boolean = false
    ) {
        scope.launch {
            mutex.withLock {
                this@BaseNavigationRouter.changingContext = true
                navigationContextSwitcher.switchContext(externalEvent, finishOtherContexts)
                internalPopAll(result, shouldChangeContext = false)
            }
        }
    }

    private suspend fun internalPopAll(result: TResult, shouldChangeContext: Boolean = true) {
        val firstScreen = _navigationScreenStack.firstOrNull()
        if (firstScreen != null) {
            _navigationScreenStack.clear()
            _eventFlow.emit(
                NavigationEvent(firstScreen, NavigationType.CLEAR_STACK)
            )
        }
        onFinishNavigation(result, shouldChangeContext = shouldChangeContext)
    }

    private fun onFinishNavigation(result: TResult, shouldChangeContext: Boolean = true) {
        if (parentNavigationRouter != null) {
            parentNavigationRouter?.onChildNavigationRouterFinished(result)
            parentNavigationRouter?.navigationScreenStack?.lastOrNull()?.screen?.screenName?.let { screenName ->
                screenNavigationTracker.onScreenChanged(
                    screenName
                )
            }
        } else {
            navigationContext?.let { context -> navigationContextSwitcher.finishContext(context, shouldChangeContext) }
        }
    }

    override fun reset() {
        if (parentNavigationRouter != null) {
            popAll(navigationResult)
            parentNavigationRouter?.reset()
        } else {
            navigationScreenStack.reversed().forEachIndexed { index, it ->
                if (index == navigationScreenStack.size - 1) {
                    return@forEachIndexed
                }
                popIfShowing(it.id, navigationResult)
            }
        }
        shouldReset = false
    }

    override fun shouldFinish(): Boolean {
        val shouldFinish = fullNavigationStackSize == 0 && finishOnEmptyStack
        finishOnEmptyStack = true
        return shouldFinish
    }

    override fun isChangingContext(): Boolean = changingContext
}