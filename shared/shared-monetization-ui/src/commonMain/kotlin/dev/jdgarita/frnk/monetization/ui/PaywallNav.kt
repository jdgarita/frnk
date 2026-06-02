package dev.jdgarita.frnk.monetization.ui

import androidx.navigation.NavGraphBuilder
import dev.jdgarita.frnk.ui.nav.ToolkitRoute
import dev.jdgarita.frnk.ui.nav.frnkComposable

/**
 * Mounts the toolkit-owned paywall at [ToolkitRoute.Paywall]. The host owns the `NavController` (P2-1)
 * but gets the paywall destination in one line:
 *
 * ```
 * FrnkNavHost(navController, startRoute) {
 *     frnkPaywallDestination(features = proFeatures, source = "home_topbar") { navigator.popBackStack() }
 *     // … other destinations
 * }
 * ```
 *
 * @param onClose called when the paywall should be dismissed (purchase/restore succeeded or the user closed it).
 * @param onMessage surfaces transient messages (e.g. "Nothing to restore", purchase failure) — wire to a toast/snackbar.
 */
fun NavGraphBuilder.frnkPaywallDestination(
    features: List<String> = emptyList(),
    source: String = "paywall",
    onMessage: (String) -> Unit = {},
    onClose: () -> Unit,
) {
    frnkComposable<ToolkitRoute.Paywall> {
        PaywallScreen(source = source, features = features) { effect ->
            when (effect) {
                PaywallEffect.Dismiss -> onClose()
                is PaywallEffect.Message -> onMessage(effect.text)
            }
        }
    }
}
