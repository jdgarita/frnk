package dev.jdgarita.frnk.monetization.ui.ext

import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.ui.platformManageSubscriptionsUrl
import dev.jdgarita.frnk.utils.AppResult

/**
 * The URL a "Manage Subscription" row should open: the billing provider's customer-specific
 * management URL when available, otherwise the platform's native subscriptions deep link
 * (Google Play on Android, the App Store on iOS). Never fails — a provider error or a `null`
 * URL (e.g. RevenueCat's Test Store, or before the first purchase syncs) degrades to the
 * platform fallback, so the row always lands somewhere useful. Open the result through
 * Compose's `LocalUriHandler` (what `rememberFrnkSettingsHandler` does), or any platform opener.
 */
suspend fun EntitlementManager.manageSubscriptionsUrl(): String =
    (managementUrl() as? AppResult.Success)?.data ?: platformManageSubscriptionsUrl()