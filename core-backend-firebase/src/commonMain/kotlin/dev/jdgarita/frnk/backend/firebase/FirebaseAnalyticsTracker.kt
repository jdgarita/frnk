package dev.jdgarita.frnk.backend.firebase

import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent

internal class FirebaseAnalyticsTracker : AnalyticsTracker {
    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>,
    ) {
        // TODO: Firebase.analytics.logEvent(event.key, params)
    }

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>,
    ) {
        // TODO: Firebase.analytics.logEvent(name, params)
    }

    override fun setUserProperty(
        key: String,
        value: String?,
    ) {
        // TODO: Firebase.analytics.setUserProperty(key, value)
    }
}
