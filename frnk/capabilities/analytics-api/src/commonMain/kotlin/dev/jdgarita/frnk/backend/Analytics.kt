package dev.jdgarita.frnk.backend

import dev.jdgarita.frnk.identity.IdentitySource

/**
 * Analytics surface used by toolkit + host. The toolkit emits a generic event vocabulary
 * (App_Opened, Paywall_Viewed, …) via [AnalyticsTracker.track]; host apps can route any
 * extra events through the same instance via [trackCustom].
 */
interface AnalyticsTracker : IdentitySource {
    fun track(
        event: ToolkitEvent,
        params: Map<String, Any?> = emptyMap()
    )

    fun trackCustom(
        name: String,
        params: Map<String, Any?> = emptyMap()
    )

    fun setUserProperty(
        key: String,
        value: String?
    )
}

/**
 * The toolkit's generic event vocabulary.
 *
 * [key] must be alphanumeric-plus-underscore and start with a letter — Firebase Analytics rejects
 * anything else (hyphens included) and silently drops the event, and `FirebaseAnalyticsTracker`
 * wraps every SDK call in `runCatching`, so a malformed key fails invisibly.
 */
enum class ToolkitEvent(
    val key: String
) {
    AppOpened("app_opened"),
    PaywallViewed("paywall_viewed"),
    PaywallDismissed("paywall_dismissed"),
    PurchaseStarted("purchase_started"),
    PurchaseCompleted("purchase_completed"),
    PurchaseFailed("purchase_failed"),
    IdentitySynced("identity_synced"),
    IdentitySyncFailed("identity_sync_failed")
}

interface CrashReporter : IdentitySource {
    fun recordException(
        throwable: Throwable,
        extras: Map<String, String> = emptyMap()
    )

    fun log(message: String)
}