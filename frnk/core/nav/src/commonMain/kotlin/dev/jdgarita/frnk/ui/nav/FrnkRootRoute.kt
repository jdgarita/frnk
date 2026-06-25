package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * The toolkit's **root-level** catalogue of type-safe Navigation3 destinations, for the lower-level
 * `FrnkApp` two-level navigation shape (`:ui-app`): full-screen/modal flows ([Onboarding], [Paywall])
 * and the tabbed shell ([Tab], which hosts its own inner [FrnkTabRoute] back stack) live at the app root.
 * Each member is a [NavKey] (the nav3 back-stack key marker) and `@Serializable` so it can be persisted
 * on the root `NavBackStack` and restored via [frnkRootNavConfig]'s `SavedStateConfiguration`.
 * [Onboarding] and [Paywall] carry [FrnkFullScreenRoute].
 *
 * **Which catalogue keys which stack:** [FrnkRootRoute] keys the **root** back stack (`FrnkApp`'s root
 * `NavDisplay`) and owns everything that appears above the bottom bar (onboarding / paywall / the tab
 * shell), while [FrnkTabRoute] keys the **nested/tab** back stack inside the [Tab] shell
 * (`FrnkNestedNavScaffold`). These are plain serializable data objects/classes — `:core-nav` stays
 * Compose-free (the nav3 runtime is pure Kotlin), so the contract is consumable by feature ViewModels
 * without dragging in `compose.runtime`. Hosts may also add their own `@Serializable` `NavKey` route
 * types; the nav engine (`FrnkNavDisplay` in `:ui-scaffolds`) is generic over any `NavKey`.
 *
 * Navigation flows through the MVI effect channel: a ViewModel emits a one-shot navigation effect and a
 * single Compose-side collector mutates the host-owned `NavBackStack` (see [navigateTo] / [back]).
 */
@Serializable
sealed interface FrnkRootRoute : NavKey {
    @Serializable
    data object Onboarding : FrnkRootRoute, FrnkFullScreenRoute

    @Serializable
    data object Tab : FrnkRootRoute

    @Serializable
    data object Paywall : FrnkRootRoute, FrnkFullScreenRoute

    @Serializable
    data class Custom(
        val id: String
    ) : FrnkRootRoute
}