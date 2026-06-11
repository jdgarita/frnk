package dev.jdgarita.frnk.demo

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation3 destinations the demo still owns. The Home / Settings tab roots and the
 * Onboarding flow moved to the toolkit defaults (`ToolkitRoute.Home` / `ToolkitRoute.Settings` /
 * `ToolkitRoute.Onboarding`) when `DemoScreen` adopted `FrnkAppShell`, and the paywall is the
 * toolkit-owned `ToolkitRoute.Paywall` — so all that's left here is the middle "Components" tab:
 * its root, and the detail screen pushed onto it.
 *
 * Each member is a `@Serializable` [NavKey] so it can key a `NavBackStack` (and restore via
 * `SavedStateConfiguration`), and so [ComponentDetail] carries a typed `name` argument.
 */
@Serializable
sealed interface DemoRoute : NavKey {
    @Serializable
    data object Components : DemoRoute

    /** Pushed detail screen for a single `Frnk*` atom, identified by its display [name]. */
    @Serializable
    data class ComponentDetail(
        val name: String,
    ) : DemoRoute
}
