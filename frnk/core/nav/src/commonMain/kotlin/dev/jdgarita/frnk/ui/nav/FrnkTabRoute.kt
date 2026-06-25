package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * The toolkit's default **tab-level** catalogue of type-safe Navigation3 destinations — the three fixed
 * tabs of `FrnkNestedNavScaffold` (`Home · <custom> · Settings`). Each member is a [NavKey] (the nav3
 * back-stack key marker) and `@Serializable` so it can be persisted on a nested `NavBackStack` and
 * restored via a [frnkNestedNavConfig]-built `SavedStateConfiguration` (which registers exactly
 * [Home] / [Custom] / [Settings]).
 *
 * **Which catalogue keys which stack:** [FrnkTabRoute] keys the **nested/tab** back stack *inside* the
 * tabbed shell ([FrnkRootRoute.Tab]); the parallel [FrnkRootRoute] catalogue keys the **root** back
 * stack and owns the app-root / full-screen flows ([FrnkRootRoute.Onboarding] / [FrnkRootRoute.Paywall] /
 * [FrnkRootRoute.Tab]). Reach for [FrnkRootRoute] for anything that should appear above the bottom bar.
 *
 * These are plain serializable data objects/classes — `:core-nav` stays Compose-free (the nav3
 * runtime is pure Kotlin), so this contract is consumable by feature ViewModels without dragging in
 * `compose.runtime`. Hosts may also declare their own `@Serializable` `NavKey` route types; the nav
 * engine (`FrnkNavDisplay` in `:ui-scaffolds`) is generic over any `NavKey`, this is just the
 * batteries-included set.
 *
 * Navigation flows through the MVI effect channel: a ViewModel emits a one-shot navigation effect and a
 * single Compose-side collector mutates the host-owned `NavBackStack` (see [navigateTo] / [back]).
 */
@Serializable
sealed interface FrnkTabRoute : NavKey {
    @Serializable
    data object Home : FrnkTabRoute

    @Serializable
    data object Settings : FrnkTabRoute

    @Serializable
    data class Custom(
        val id: String
    ) : FrnkTabRoute
}