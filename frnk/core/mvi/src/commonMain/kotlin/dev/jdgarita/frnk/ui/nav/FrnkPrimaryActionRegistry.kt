package dev.jdgarita.frnk.ui.nav

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Routes the bottom bar's **primary-action button** (the "Create/Add" FAB) to the currently active
 * screen, so the screen's ViewModel — not the host root — decides what tapping it does.
 *
 * The scaffold that renders the button (`FrnkTabbedNavScaffold` in `:shared-ui-nav`) observes
 * [active]: the button shows only while a handler is registered (or the host supplied a root-level
 * fallback) and invokes the registered handler on tap. A screen claims the button for its lifetime
 * via the `FrnkPrimaryActionHandler` composable in `:shared-ui-atoms`, whose canonical body sends an
 * MVI intent — `FrnkPrimaryActionHandler { onIntent(HomeIntent.PrimaryActionClicked) }` — keeping the
 * click on the strict state-driven path.
 *
 * Registrations form a **stack**: the last registered handler wins and unregistering restores the
 * previous one. That covers the brief overlap during a navigation transition where the outgoing and
 * incoming screens are both composed; in steady state the nav display composes only the top entry,
 * so at most one screen is registered. Main-thread only (registration is composition-driven), like
 * the rest of the nav contract.
 *
 * Compose-free on purpose (mirrors [FrnkPendingRouteRequest]) so the contract lives in
 * `:shared-ui-api` and ViewModels/tests can reason about it without `compose.runtime`.
 */
class FrnkPrimaryActionRegistry {
    private val entries = mutableListOf<Entry>()
    private val _active = MutableStateFlow<(() -> Unit)?>(null)

    /** The currently active handler — last registered wins; `null` while no screen claims the button. */
    val active: StateFlow<(() -> Unit)?> = _active.asStateFlow()

    /** Pushes [handler] onto the stack and returns the token that pops it again. */
    fun register(handler: () -> Unit): FrnkPrimaryActionRegistration {
        val entry = Entry(handler)
        entries += entry
        recompute()
        return FrnkPrimaryActionRegistration {
            entries.remove(entry)
            recompute()
        }
    }

    private fun recompute() {
        _active.value = entries.lastOrNull()?.handler
    }

    private class Entry(
        val handler: () -> Unit,
    )
}

/** Token returned by [FrnkPrimaryActionRegistry.register]; [unregister] is idempotent. */
fun interface FrnkPrimaryActionRegistration {
    fun unregister()
}
