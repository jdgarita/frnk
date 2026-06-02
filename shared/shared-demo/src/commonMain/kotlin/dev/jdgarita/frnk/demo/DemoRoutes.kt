package dev.jdgarita.frnk.demo

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the demo, driving the toolkit's `FrnkNavHost`. The three tab roots
 * ([Home] / [Components] / [Settings]) are top-level destinations switched via the floating bottom
 * bar (each keeps its own saved back stack); [ComponentDetail] and [Onboarding] are pushed full
 * screens with a back arrow. The paywall is the toolkit-owned `ToolkitRoute.Paywall` (mounted via
 * `frnkPaywallDestination`), not a `DemoRoute`.
 *
 * Each member is `@Serializable` so navigation-compose can encode it into the back stack and so
 * [ComponentDetail] can carry a typed `name` argument instead of a stringly-keyed bundle.
 */
@Serializable
sealed interface DemoRoute {
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
