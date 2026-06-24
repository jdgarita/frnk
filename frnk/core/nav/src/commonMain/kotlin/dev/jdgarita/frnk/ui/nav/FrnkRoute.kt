package dev.jdgarita.frnk.ui.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * The toolkit's default **tab-level** catalogue of type-safe Navigation3 destinations. Each member is a
 * [NavKey] (the nav3 back-stack key marker) and `@Serializable` so it can be persisted on a
 * `NavBackStack` and restored via a [frnkNestedNavConfig]-built `SavedStateConfiguration` (the
 * `Home`/`Custom`/`Settings` members are the ones it registers). For the lower-level `FrnkApp` root path,
 * the app-root/full-screen flows live on the parallel [FrnkRootRoute] catalogue instead.
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
sealed interface FrnkRoute : NavKey {
    @Serializable
    data object Home : FrnkRoute

    @Serializable
    data object Onboarding : FrnkRoute, FrnkFullScreenRoute

    @Serializable
    data object Settings : FrnkRoute

    @Serializable
    data object Paywall : FrnkRoute, FrnkFullScreenRoute

    @Serializable
    data class Custom(
        val id: String
    ) : FrnkRoute
}