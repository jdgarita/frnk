package dev.jdgarita.frnk.monetization.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.ui.haptics.HAPTICS_TOGGLE_ID
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.nav.FrnkNavigator
import dev.jdgarita.frnk.ui.nav.ToolkitRoute
import dev.jdgarita.frnk.ui.scaffolds.SettingsAction
import dev.jdgarita.frnk.ui.scaffolds.SettingsEffect
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Stable id for the god-mode toggle row, so the handler can recognize it among settings toggles. */
const val GOD_MODE_TOGGLE_ID = "god_mode"

/**
 * Centralizes the toolkit's Settings wiring so every host gets paywall navigation, restore,
 * manage-subscription, the god-mode toggle, and the haptic-feedback toggle for free. Handles
 * `UpgradeToPro` (→ navigate to the toolkit [ToolkitRoute.Paywall]), `RestorePurchases`,
 * `ManageSubscription` (→ open the provider's customer-specific management URL, falling back to the
 * platform's native subscriptions deep link via [platformManageSubscriptionsUrl]), and the god-mode and
 * `HAPTICS_TOGGLE_ID` [SettingsEffect.ToggleChanged]s (the latter drives the ambient [LocalFrnkHaptics]);
 * delegates everything else (theme appearance, other actions) to [fallback].
 */
@Composable
fun rememberFrnkSettingsHandler(
    navigator: FrnkNavigator,
    entitlements: EntitlementManager,
    analytics: AnalyticsTracker,
    onMessage: (String) -> Unit = {},
    fallback: (SettingsEffect) -> Unit = {},
): (SettingsEffect) -> Unit {
    val scope: CoroutineScope = rememberCoroutineScope()
    val uriHandler: UriHandler = LocalUriHandler.current
    val haptics = LocalFrnkHaptics.current
    return remember(navigator, entitlements, analytics, uriHandler, haptics, onMessage, fallback) {
        { effect ->
            when (effect) {
                is SettingsEffect.ActionInvoked ->
                    when (effect.action) {
                        SettingsAction.UpgradeToPro -> {
                            analytics.track(ToolkitEvent.PaywallViewed, mapOf("source" to "settings"))
                            navigator.navigate(ToolkitRoute.Paywall)
                        }
                        SettingsAction.RestorePurchases ->
                            scope.launch {
                                val restored = entitlements.restorePurchases()
                                onMessage(restoreMessage(restored is AppResult.Success && restored.data))
                            }
                        SettingsAction.ManageSubscription -> {
                            analytics.trackCustom("manage_subscription_opened")
                            scope.launch {
                                // Prefer the provider's customer-specific management URL; fall back to the
                                // platform's native subscriptions deep link (Play / App Store) so the row
                                // always lands somewhere useful even when the provider has no URL (e.g.
                                // RevenueCat's Test Store, or a provider failure).
                                val url =
                                    (entitlements.managementUrl() as? AppResult.Success)?.data
                                        ?: platformManageSubscriptionsUrl()
                                uriHandler.openUri(url)
                            }
                        }
                        else -> fallback(effect)
                    }
                is SettingsEffect.ToggleChanged ->
                    when (effect.id) {
                        GOD_MODE_TOGGLE_ID -> entitlements.setGodMode(effect.checked)
                        HAPTICS_TOGGLE_ID -> haptics.setEnabled(effect.checked)
                        else -> fallback(effect)
                    }
                is SettingsEffect.AppearanceChanged -> fallback(effect)
            }
        }
    }
}

private fun restoreMessage(restoredToPro: Boolean): String = if (restoredToPro) "Purchases restored" else "Nothing to restore"
