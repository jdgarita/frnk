package dev.jdgarita.frnk.monetization.ui

/**
 * The App Store's "Manage Subscriptions" screen. This https URL opens the system subscriptions UI on
 * iOS (equivalent to Settings → Apple ID → Subscriptions); StoreKit also exposes a native sheet, but
 * the URL keeps this layer free of a StoreKit dependency and works through `LocalUriHandler`.
 */
internal actual fun platformManageSubscriptionsUrl(): String = "https://apps.apple.com/account/subscriptions"
