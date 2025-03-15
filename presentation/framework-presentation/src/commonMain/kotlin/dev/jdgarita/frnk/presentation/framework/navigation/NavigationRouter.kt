package dev.jdgarita.frnk.presentation.framework.navigation

import dev.jdgarita.frnk.presentation.mvi.ExternalEvent
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.SharedFlow

/**
 * The navigation router system is based on the Coordinator pattern.
 *
 * A single navigation router is responsible for representing the flow of a set of screens
 * specific to a single domain.  They emit navigation event updates for changes to the view hierarchy.
 *
 * Native platforms should have a view system that monitors these navigation events and render the navigation
 * hierarchy changes provided.  Native platform view systems may also affect the hierarchy when views are
 *
 * For each portion of an application that has a navigation hierachy (navigation tab, main panel, side panel, etc),
 * there should be one active navigation router.  When navigation needs to be handled by
 * a new navigation router, this *child* navigation router should be pushed on top of the
 * previous navigation router (now parent) and become active.  When the child navigation router
 * finishes its navigation handling, it will return control of the navigation to the parent after having
 * popped all of its views.
 *
 * A navigation router gets [start] called when it should take control.  When it is finished, it
 * should call [onChildNavigationRouterFinished] on its parent.
 *
 *
 */
interface NavigationRouter {
    val navigationScreenStack: List<ScreenEntry>

    /**
     * The total number of screens in the navigation stack, including child navigation routers.
     */
    val fullNavigationStackSize: Int

    var isStarted: Boolean

    /**
     * Whether the navigation router should reset to its initial state with only the start destination.
     */
    var shouldReset: Boolean

    val activeNavigationRouter: NavigationRouter

    val eventFlow: SharedFlow<NavigationEvent>

    fun <T : ExternalEvent> registerNavigationHandler(
        externalEvent: KClass<out T>,
        navigationRouterProvider: () -> NavigationRouter
    )

    /**
     * Start handling navigation for root router that has no parent in a [NavigationContext].
     */
    fun start(externalEvent: ExternalEvent, navigationContext: NavigationContext)

    /**
     * Start handling navigation. Called by parent.
     */
    fun start(externalEvent: ExternalEvent, parentNavigationRouter: NavigationRouter)

    /**
     * Called when the child navigation router finishes
     */
    fun onChildNavigationRouterFinished(result: NavigationRouterResult)

    /**
     * Handle external event from a ViewModel.  Should only need to be called on the root
     * navigation router
     */
    fun handleExternalEvent(externalEvent: ExternalEvent)

    /**
     * Reset the navigation router to its initial state with only the start destination.
     */
    fun reset()

    /**
     * Indicates whether the navigation router should be finished or not
     */
    fun shouldFinish(): Boolean

    /**
     * Indicates whether the navigation router is changing context. It is used to determine if the
     * app should be finished when the navigation router is finished.
     */
    fun isChangingContext(): Boolean
}