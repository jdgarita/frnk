package dev.jdgarita.frnk.monetization.ui

/**
 * The platform's native subscription-management deep link — the page where a user reviews and cancels
 * their subscriptions (Google Play on Android, App Store on iOS). [rememberFrnkSettingsHandler] opens
 * this as a fallback for `ManageSubscription` when the billing provider returns no customer-specific
 * management URL (e.g. RevenueCat's Test Store, or before the first purchase syncs).
 *
 * Returned as a URL string so it can be handed to Compose's `LocalUriHandler` — the same cross-platform
 * open-URL seam the toolkit already uses — without threading a platform `Context`/`UIApplication` through.
 */
internal expect fun platformManageSubscriptionsUrl(): String
