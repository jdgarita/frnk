package dev.jdgarita.frnk.backend

/**
 * Analytics surface used by toolkit + host. The toolkit emits a generic event vocabulary
 * (App_Opened, Paywall_Viewed, …) via [AnalyticsTracker.track]; host apps can route any
 * extra events through the same instance via [trackCustom].
 */
interface AnalyticsTracker {
    fun track(event: ToolkitEvent, params: Map<String, Any?> = emptyMap())
    fun trackCustom(name: String, params: Map<String, Any?> = emptyMap())
    fun setUserProperty(key: String, value: String?)
}

enum class ToolkitEvent(val key: String) {
    AppOpened("App_Opened"),
    PaywallViewed("Paywall_Viewed"),
    PaywallDismissed("Paywall_Dismissed"),
    PurchaseStarted("Purchase_Started"),
    PurchaseCompleted("Purchase_Completed"),
    PurchaseFailed("Purchase_Failed"),
    SignInStarted("SignIn_Started"),
    SignInCompleted("SignIn_Completed"),
}

interface CrashReporter {
    fun recordException(throwable: Throwable, extras: Map<String, String> = emptyMap())
    fun setUserId(id: String?)
    fun log(message: String)
}
