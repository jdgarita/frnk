package dev.jdgarita.frnk.backend.supabase

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent

/**
 * Supabase has no first-party analytics product; host apps that want events should provide
 * their own [AnalyticsTracker] binding (e.g. Mixpanel, PostHog). This no-op is the safe default.
 */
internal class NoopAnalyticsTracker : AnalyticsTracker {
    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>,
    ) = Unit

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>,
    ) = Unit

    override fun setUserProperty(
        key: String,
        value: String?,
    ) = Unit
}
