package dev.jdgarita.frnk.demo

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation3 destinations for the demo. The three tab roots ([Home] / [Components] /
 * [Settings]) are switched via the floating bottom bar (each keeps its own back stack through
 * `rememberFrnkTabbedBackStacks`); [ComponentDetail] is pushed onto the Components tab and [Onboarding]
 * onto the Settings tab, both as full screens with a back arrow. The paywall is the toolkit-owned
 * `ToolkitRoute.Paywall` (registered via `frnkPaywallNavigation`), not a `DemoRoute`.
 *
 * Each member is a `@Serializable` [NavKey] so it can key a `NavBackStack` (and restore via
 * `SavedStateConfiguration`), and so [ComponentDetail] carries a typed `name` argument.
 */
@Serializable
sealed interface DemoRoute : NavKey {
    @Serializable
    data object Home : DemoRoute

    @Serializable
    data object Components : DemoRoute

    @Serializable
    data object Settings : DemoRoute

    /** Pushed detail screen for a single `Frnk*` atom, identified by its display [name]. */
    @Serializable
    data class ComponentDetail(
        val name: String,
    ) : DemoRoute

    @Serializable
    data object Onboarding : DemoRoute
}
