package dev.jdgarita.frnk.monetization.ui

/**
 * The platform's native subscription-management deep link — the page where a user reviews and cancels
 * their subscriptions (Google Play on Android, App Store on iOS). The public
 * `EntitlementManager.manageSubscriptionsUrl()` extension (`ext/EntitlementManagerExt.kt`) uses this as
 * the fallback when the billing provider returns no customer-specific management URL (e.g. RevenueCat's
 * Test Store, or before the first purchase syncs); [rememberFrnkSettingsHandler] opens it for
 * `ManageSubscription`.
 *
 * Returned as a URL string so it can be handed to Compose's `LocalUriHandler` — the same cross-platform
 * open-URL seam the toolkit already uses — without threading a platform `Context`/`UIApplication` through.
 */
internal expect fun platformManageSubscriptionsUrl(): String