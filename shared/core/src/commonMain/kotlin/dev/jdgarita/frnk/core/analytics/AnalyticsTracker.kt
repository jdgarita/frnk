package dev.jdgarita.frnk.core.analytics

/**
 * @author Vivien Mahe
 * @since 18/02/2025
 */
class AnalyticsTracker(
    private val isDebug: Boolean,
    private val analyticsProviders: List<AnalyticsProvider>,
) {

    fun identifyUser(userId: String) {
        if (isDebug) return

        analyticsProviders.forEach { it.identifyUser(userId = userId) }
    }

    fun trackEvent(event: AnalyticsEvent, properties: Map<String, Any>? = null) {
        if (isDebug) return

        analyticsProviders.forEach { it.trackEvent(eventName = event.name, properties = properties) }
    }

    fun clear() {
        if (isDebug) return

        analyticsProviders.forEach { it.clear() }
    }
}
