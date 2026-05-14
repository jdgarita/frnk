package dev.jdgarita.frnk.ui.nav

/**
 * Generic route catalogue surfaced by the toolkit. Host apps MAP these to concrete
 * Compose destinations in their own NavHost. The toolkit never owns the NavHost.
 */
sealed interface ToolkitRoute {
    data object Home : ToolkitRoute
    data object Settings : ToolkitRoute
    data object Paywall : ToolkitRoute
    data object SignIn : ToolkitRoute
    data object SignUp : ToolkitRoute
    data class Custom(val id: String) : ToolkitRoute
}

/** A function the host wires up to launch a toolkit-defined navigation. */
typealias Navigator = (ToolkitRoute) -> Unit
