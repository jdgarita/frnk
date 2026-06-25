package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Cross-process signal for deep-linking directly into a [NavKey] destination on app launch.
 *
 * An external entry point (e.g. a home-screen widget, a notification, a push intent) sets a pending
 * route before the root composable is attached; the composable observes [pending], pushes the route
 * onto the back stack (see [navigateTo]), and calls [consume] to clear the signal.
 *
 * State-based (not event-based) on purpose: if the signal is set before the observer attaches, it still
 * delivers — a `SharedFlow` with `replay = 0` would miss the emission in that race.
 *
 * Generic over [NavKey] so any host route (a [FrnkTabRoute]/[FrnkRootRoute] member or a host-defined route) can be the
 * deep-link target. Register it as a singleton in the host's DI graph.
 */
class FrnkPendingRouteRequest {
    private val _pending = MutableStateFlow<NavKey?>(null)
    val pending: StateFlow<NavKey?> = _pending.asStateFlow()

    fun request(route: NavKey) {
        _pending.value = route
    }

    fun consume() {
        _pending.value = null
    }
}