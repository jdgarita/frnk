package dev.jdgarita.frnk.monetization.ui

import androidx.compose.runtime.Composable
import dev.jdgarita.frnk.ui.nav.FrnkRootRoute
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.Module
import org.koin.dsl.navigation3.navigation

/**
 * The toolkit-owned paywall as a **ready-to-mount Navigation3 destination body** — the paywall UI plus its
 * effect wiring, decoupled from any concrete route so a host can mount it against [FrnkRootRoute.Paywall]
 * (via [frnkPaywallNavigation]) **or** its own route:
 *
 * ```
 * // host with its own route:
 * navigation<MyRoute.Paywall> { FrnkPaywallDestination(features = proFeatures, source = "home") { backStack.back() } }
 * ```
 *
 * @param onClose called when the paywall should be dismissed (purchase/restore succeeded or user closed it).
 * @param onMessage surfaces transient messages (e.g. "Nothing to restore", purchase failure) — wire to a toast/snackbar.
 */
@Composable
fun FrnkPaywallDestination(
    features: List<String> = emptyList(),
    source: String = "paywall",
    onMessage: (String) -> Unit = {},
    onClose: () -> Unit
) {
    PaywallScreen(source = source, features = features) { effect ->
        when (effect) {
            PaywallEffect.Dismiss -> onClose()
            is PaywallEffect.Message -> onMessage(effect.text)
        }
    }
}

/**
 * Registers [FrnkPaywallDestination] at the toolkit-owned [FrnkRootRoute.Paywall] in a Koin navigation
 * module — the one-liner real hosts use (the same route `rememberFrnkSettingsHandler` navigates to). The
 * host resolves destinations through `FrnkNavDisplay`'s default `koinEntryProvider()`:
 *
 * ```
 * val appNavModule = module { frnkPaywallNavigation(features = proFeatures, source = "settings") { backStack.back() } }
 * ```
 */
@OptIn(KoinExperimentalAPI::class)
fun Module.frnkPaywallNavigation(
    features: List<String> = emptyList(),
    source: String = "paywall",
    onMessage: (String) -> Unit = {},
    onClose: () -> Unit
) {
    navigation<FrnkRootRoute.Paywall> {
        FrnkPaywallDestination(features = features, source = source, onMessage = onMessage, onClose = onClose)
    }
}