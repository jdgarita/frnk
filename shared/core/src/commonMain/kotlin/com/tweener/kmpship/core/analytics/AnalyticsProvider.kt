package com.tweener.kmpship.core.analytics

/**
 * @author Vivien Mahe
 * @since 18/02/2025
 */
interface AnalyticsProvider {

    fun initialize(isDebug: Boolean)

    fun identifyUser(userId: String)

    fun trackEvent(eventName: String, properties: Map<String, Any>? = null)

    fun clear()
}
