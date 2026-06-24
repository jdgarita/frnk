package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/*
 * Navigation3 back-stack helpers — the toolkit's vocabulary for driving navigation. In nav3 the
 * host owns the back stack (a `NavBackStack<NavKey>`, which is a plain `MutableList<NavKey>`), so
 * "navigating" is mutating that list. These extensions name the common mutations so feature code reads
 * intent-first instead of poking the list, and stay Compose-free (callable from MVI effect handlers).
 *
 * Drive them from a single Compose-side collector of the ViewModel's one-shot effect channel (the
 * channel is single-consumer) — typically inside each `navigation<Route> { … }` block or a single effect
 * collector (e.g. `FrnkScreen`'s `onEffect`).
 */

/**
 * Push [screen] onto the back stack. If [popScreen] is given, it is first removed from the stack —
 * the "navigate while dropping another entry" pattern (e.g. Settings → Onboarding, removing Settings so
 * back doesn't return to it).
 *
 * [singleTop] (default `true`) is the nav2 `launchSingleTop` equivalent: if [screen] already equals the
 * current top entry, the push is skipped so a doubly-fired navigation effect (a rapid double-tap, or an
 * effect re-emitted on recomposition) can't stack two identical destinations. It compares only the top
 * entry, so pushing two *distinct* instances of the same route type (e.g. `Detail("a")` then
 * `Detail("b")`) is unaffected. Pass `singleTop = false` for the rare flow that intentionally pushes a
 * duplicate of the current destination.
 */
fun NavBackStack<NavKey>.navigateTo(
    screen: NavKey,
    popScreen: NavKey? = null,
    singleTop: Boolean = true
) {
    popScreen?.let { remove(it) }
    if (singleTop && lastOrNull() == screen) return
    add(screen)
}

/** Pop the top destination. No-op on an empty stack. Wire to `NavDisplay`'s `onBack`. */
fun NavBackStack<NavKey>.back() {
    removeLastOrNull()
}

/**
 * Clear the entire stack and push [screen] as the sole entry — the finished-flow reset (e.g. onboarding
 * completes → land on the main screen with no way back into onboarding).
 */
fun NavBackStack<NavKey>.clearAndNavigateTo(screen: NavKey) {
    clear()
    add(screen)
}