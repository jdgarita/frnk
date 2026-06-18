package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Stable
import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.nav.clearAndNavigateTo
import dev.jdgarita.frnk.ui.nav.navigateTo

/**
 * Handle `FrnkTabbedNavScaffold` passes to every host extension point (`effects`, `entries`, the effect
 * handlers): the scaffold-owned navigation state, so a host's single `EffectCollector` can drive
 * navigation (`scope.navigateTo(route)`) without owning the back-stack wiring itself.
 */
@Stable
class FrnkAppScope internal constructor(
    /** The per-tab back stacks the scaffold created — `tabbed.current` is the active tab's stack. */
    val tabbed: FrnkTabbedBackStacks
) {
    /** Push [route] onto the active tab's back stack (single-top, like [navigateTo]). */
    fun navigateTo(route: NavKey) {
        tabbed.current.navigateTo(route)
    }

    /** Pop the active tab's back stack. */
    fun back() {
        tabbed.current.back()
    }

    /** Reset the active tab to [route] alone — for finished flows ([clearAndNavigateTo]). */
    fun clearAndNavigateTo(route: NavKey) {
        tabbed.current.clearAndNavigateTo(route)
    }
}