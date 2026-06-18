package dev.jdgarita.frnk.monetization.ui

/**
 * Google Play's subscriptions screen. The bare `…/account/subscriptions` deep link opens the user's
 * full subscription list; the Play Store app intercepts this https URL on-device.
 */
internal actual fun platformManageSubscriptionsUrl(): String = "https://play.google.com/store/account/subscriptions"