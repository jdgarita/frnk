package dev.jdgarita.frnk.ui.nav

import kotlinx.serialization.Serializable

/**
 * The toolkit's default catalogue of type-safe navigation routes. Each member is
 * `@Serializable` so it can be used as a destination with JetBrains CMP navigation-compose
 * (the toolkit's `FrnkNavHost` primitives in `:shared-ui-atoms` build the `NavHost`; the
 * **host** owns the `NavController`/back-stack instance and passes it in).
 *
 * These are plain serializable data objects/classes — `:shared-ui-api` stays Compose-free,
 * so this contract is consumable by feature ViewModels without dragging in `compose.runtime`.
 * Hosts may also declare their own `@Serializable` route types; the nav primitives are generic
 * over any serializable route, this is just the batteries-included set.
 *
 * Navigation flows through the MVI effect channel: a ViewModel emits a navigation effect and a
 * single collector routes it into a [FrnkNavigator].
 */
@Serializable
sealed interface ToolkitRoute {
    @Serializable
    data object Home : ToolkitRoute

    @Serializable
    data object Settings : ToolkitRoute

    @Serializable
    data object Paywall : ToolkitRoute

    @Serializable
    data object SignIn : ToolkitRoute

    @Serializable
    data object SignUp : ToolkitRoute

    @Serializable
    data class Custom(
        val id: String,
    ) : ToolkitRoute
}
