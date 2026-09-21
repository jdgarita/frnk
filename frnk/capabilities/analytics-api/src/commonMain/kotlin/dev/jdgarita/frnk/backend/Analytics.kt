package dev.jdgarita.frnk.backend

import dev.jdgarita.frnk.identity.IdentitySource

/**
 * Analytics surface used by toolkit + host. The toolkit emits a generic event vocabulary
 * (App_Opened, Paywall_Viewed, …) via [AnalyticsTracker.track]; host apps can route any
 * extra events through the same instance via [trackCustom], and report navigation through
 * [screen] so a provider can use its native screen-view primitive.
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

    /**
     * A screen became visible. Providers map this onto their own screen-view primitive (PostHog's
     * `screen`, Firebase's `screen_view` event) rather than a custom event, so the provider's
     * navigation reports and session views understand it. [name] is the host's stable screen id.
     */
    fun screen(
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
 * [key] is lowercase `snake_case`: letters, digits and underscores, starting with a letter. That
 * is the intersection every provider accepts — Firebase Analytics silently drops anything else
 * (hyphens included), and PostHog reserves the `$` prefix for its own events and properties.
 * Every tracker wraps its SDK calls in `runCatching`, so a malformed key fails invisibly.
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

    /** A RevenueCat web-purchase redemption link was redeemed; `result` says how it went. */
    WebPurchaseRedeemed("web_purchase_redeemed"),
    IdentitySynced("identity_synced"),
    IdentitySyncFailed("identity_sync_failed")
}

/**
 * Crash-reporting surface. [recordException] is the non-fatal primitive — [extras] travel with
 * that one report only. [log] is the breadcrumb primitive: a line of context attached to whatever
 * report comes next, never a report of its own.
 */
interface CrashReporter : IdentitySource {
    fun recordException(
        throwable: Throwable,
        extras: Map<String, String> = emptyMap()
    )

    fun log(message: String)
}