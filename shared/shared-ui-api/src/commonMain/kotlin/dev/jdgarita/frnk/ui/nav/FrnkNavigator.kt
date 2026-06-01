package dev.jdgarita.frnk.ui.nav

/**
 * Compose-free navigation abstraction the toolkit exposes so feature code (and MVI effect
 * handlers) can drive navigation without depending on `compose.runtime` or navigation-compose.
 *
 * The concrete implementation lives in `:shared-ui-atoms` (`rememberFrnkNavigator`), backed by a
 * host-owned `NavController`. Navigation is typically emitted as a one-shot MVI effect and routed
 * here by a single collector — see `EffectCollector` in `:shared-ui-atoms`.
 *
 * [navigate] mirrors `NavController.navigate(route: Any)`: a route is any `@Serializable`
 * destination object (e.g. a [ToolkitRoute] member or a host-defined route).
 */
interface FrnkNavigator {
    /** Push [route] onto the back stack. */
    fun navigate(route: Any)

    /**
     * Navigate to [route] applying [options] — e.g. top-level tab switching (each tab keeps its own
     * saved sub-back-stack) or clearing a flow off the stack (`popUpTo` with `inclusive = true`).
     */
    fun navigate(
        route: Any,
        options: FrnkNavOptions,
    )

    /** Pop up the back stack toward the start, honoring saved-state behavior. Returns false if it could not. */
    fun navigateUp(): Boolean

    /** Pop the top destination off the back stack. Returns false if the stack could not be popped. */
    fun popBackStack(): Boolean
}

/**
 * Compose-free description of how a [FrnkNavigator.navigate] call should affect the back stack. The
 * atoms-layer adapter translates these into navigation-compose `NavOptions`.
 *
 * @param popUpTo pop the back stack up to a route before navigating (see [PopUpTo]); `null` leaves the
 *   stack untouched and simply pushes.
 * @param launchSingleTop avoid stacking a duplicate copy of the target route on top of itself.
 * @param restoreState restore the target route's previously-[PopUpTo.saveState]d back stack if present.
 *   Save and restore are paired *across* calls: the idiomatic bottom-nav tab switch saves the leaving
 *   tab's stack (via [popUpTo] + [PopUpTo.saveState]) and restores the entering tab's, so each tab keeps
 *   its own sub-back-stack. Setting `restoreState` with no prior save is a harmless no-op.
 */
data class FrnkNavOptions(
    val popUpTo: PopUpTo? = null,
    val launchSingleTop: Boolean = false,
    val restoreState: Boolean = false,
) {
    /**
     * Pop the back stack up to [route] before navigating. Keeping [saveState] inside this block means
     * saving a back stack is only expressible together with the pop that produces it — it can't be set
     * in isolation where it would be silently ignored.
     *
     * @param route the destination to pop up to.
     * @param inclusive also pop [route] itself (use to clear a finished flow — e.g. onboarding/sign-in —
     *   entirely off the stack). Defaults to keeping [route].
     * @param saveState save the state of the popped destinations so a later `restoreState` navigate can
     *   bring them back (the multiple-back-stack tab pattern).
     */
    data class PopUpTo(
        val route: Any,
        val inclusive: Boolean = false,
        val saveState: Boolean = false,
    )
}
