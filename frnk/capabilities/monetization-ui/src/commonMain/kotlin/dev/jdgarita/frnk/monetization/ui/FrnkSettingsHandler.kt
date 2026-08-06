package dev.jdgarita.frnk.monetization.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.ui.ext.manageSubscriptionsUrl
import dev.jdgarita.frnk.ui.haptics.HAPTICS_TOGGLE_ID
import dev.jdgarita.frnk.ui.haptics.LocalFrnkHaptics
import dev.jdgarita.frnk.ui.nav.FrnkRootRoute
import dev.jdgarita.frnk.ui.nav.navigateTo
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsAction
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsEffect
import dev.jdgarita.frnk.utils.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Stable id for the god-mode toggle row, so the handler can recognize it among settings toggles. */
const val GOD_MODE_TOGGLE_ID = "god_mode"

/**
 * Centralizes the toolkit's Settings wiring so every host gets paywall navigation, restore,
 * manage-subscription, the god-mode toggle, and the haptic-feedback toggle for free. Handles
 * `UpgradeToPro` (→ navigate to the toolkit [FrnkRootRoute.Paywall]), `RestorePurchases`,
 * `ManageSubscription` (→ open [manageSubscriptionsUrl] — the provider's customer-specific management
 * URL, falling back to the platform's native subscriptions deep link), and the god-mode and
 * `HAPTICS_TOGGLE_ID` [SettingsEffect.ToggleChanged]s (the latter drives the ambient [LocalFrnkHaptics]);
 * delegates everything else (theme appearance, other actions) to [fallback].
 *
 * @param backStack the **root** back stack — the paywall is a full-screen flow ([FrnkRootRoute.Paywall],
 *   marked `FrnkFullScreenRoute`), so it must land above the bottom bar, not pushed onto a tab's stack.
 *   (Mirrors `FrnkTabNavigator.openPaywall()`, which also pushes the paywall onto the root stack.)
 */
@Composable
fun rememberFrnkSettingsHandler(
    backStack: NavBackStack<NavKey>,
    entitlements: EntitlementManager,
    analytics: AnalyticsTracker,
    onMessage: (String) -> Unit = {},
    fallback: (SettingsEffect) -> Unit = {}
): (SettingsEffect) -> Unit {
    val scope: CoroutineScope = rememberCoroutineScope()
    val uriHandler: UriHandler = LocalUriHandler.current
    val haptics = LocalFrnkHaptics.current
    return remember(backStack, entitlements, analytics, uriHandler, haptics, onMessage, fallback) {
        { effect ->
            when (effect) {
                is SettingsEffect.ActionInvoked ->
                    when (effect.action) {
                        SettingsAction.UpgradeToPro -> {
                            analytics.track(ToolkitEvent.PaywallViewed, mapOf("source" to "settings"))
                            backStack.navigateTo(FrnkRootRoute.Paywall)
                        }
                        SettingsAction.RestorePurchases ->
                            scope.launch {
                                val restored = entitlements.restorePurchases()
                                onMessage(restoreMessage(restored is AppResult.Success && restored.data))
                            }
                        SettingsAction.ManageSubscription -> {
                            analytics.trackCustom("manage_subscription_opened")
                            scope.launch {
                                uriHandler.openUri(entitlements.manageSubscriptionsUrl())
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