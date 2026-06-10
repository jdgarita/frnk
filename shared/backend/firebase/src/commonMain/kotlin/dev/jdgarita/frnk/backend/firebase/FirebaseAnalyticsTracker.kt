package dev.jdgarita.frnk.backend.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.utils.PrintLogger

/**
 * Firebase Analytics binding (gitlive `firebase-analytics`).
 *
 * Every SDK call is wrapped in [runCatching]: a host that selects Firebase observability but hasn't
 * finished wiring `google-services.json` / `GoogleService-Info.plist` degrades to a logged no-op
 * instead of crashing (BACKLOG P1-5 — "no-op when Firebase isn't configured").
 */
internal class FirebaseAnalyticsTracker : AnalyticsTracker {
    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>,
    ) = logEvent(event.key, params)

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>,
    ) = logEvent(name, params)

    override fun setUserProperty(
        key: String,
        value: String?,
    ) {
        runCatching { Firebase.analytics.setUserProperty(key, value.orEmpty()) }
            .onFailure { PrintLogger.w(TAG, "setUserProperty($key) skipped: ${it.message}") }
    }

    private fun logEvent(
        name: String,
        params: Map<String, Any?>,
    ) {
        runCatching { Firebase.analytics.logEvent(name, params.toFirebaseParams()) }
            .onFailure { PrintLogger.w(TAG, "logEvent($name) skipped: ${it.message}") }
    }

    /**
     * Firebase event params accept only String / Long / Double. Drop nulls and coerce everything
     * else into one of those (Int→Long, Float→Double, Boolean/other→String).
     */
    private fun Map<String, Any?>.toFirebaseParams(): Map<String, Any> =
        buildMap {
            this@toFirebaseParams.forEach { (key, value) ->
                when (value) {
                    null -> Unit
                    is String -> put(key, value)
                    is Long -> put(key, value)
                    is Double -> put(key, value)
                    is Int -> put(key, value.toLong())
                    is Float -> put(key, value.toDouble())
                    else -> put(key, value.toString())
                }
            }
        }

    private companion object {
        const val TAG = "FirebaseAnalytics"
    }
}
