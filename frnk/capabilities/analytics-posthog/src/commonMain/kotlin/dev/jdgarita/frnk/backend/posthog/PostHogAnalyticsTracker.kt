package dev.jdgarita.frnk.backend.posthog

import com.posthog.kmp.PostHog
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.backend.ToolkitEvent
import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.utils.AppResult
import dev.jdgarita.frnk.utils.PrintLogger

/** The SDK calls the tracker makes, behind a seam so the mapping is testable without `PostHog.setup`. */
internal interface PostHogGateway {
    fun capture(
        event: String,
        properties: Map<String, Any>
    )

    fun screen(
        name: String,
        properties: Map<String, Any>
    )

    fun identify(distinctId: String)

    fun setPersonProperties(properties: Map<String, Any>)
}

internal object PostHogSdkGateway : PostHogGateway {
    override fun capture(
        event: String,
        properties: Map<String, Any>
    ) = PostHog.capture(event = event, properties = properties)

    override fun screen(
        name: String,
        properties: Map<String, Any>
    ) = PostHog.screen(screenName = name, properties = properties)

    override fun identify(distinctId: String) = PostHog.identify(distinctId = distinctId)

    override fun setPersonProperties(properties: Map<String, Any>) = PostHog.setPersonProperties(userPropertiesToSet = properties)
}

/**
 * [AnalyticsTracker] over the PostHog KMP SDK.
 *
 * Events go through `capture`, screens through PostHog's own `screen` (the `$screen` event its
 * navigation insights read), user properties through `setPersonProperties` (`$set`), and `identify`
 * through `PostHog.identify(distinctId)` — with `personProfiles = IDENTIFIED_ONLY` that is the call
 * that creates the person, merging the device's anonymous events into it. Null parameter values are
 * dropped. Every SDK call is wrapped in [runCatching] so an SDK that failed to start degrades to a
 * logged no-op.
 */
internal class PostHogAnalyticsTracker(
    private val gateway: PostHogGateway = PostHogSdkGateway
) : AnalyticsTracker {
    override suspend fun identify(id: String): AppResult<Unit, IdentityError> =
        runCatching { gateway.identify(id) }
            .fold(
                onSuccess = { AppResult.Success(Unit) },
                onFailure = {
                    PrintLogger.w(TAG, "identify skipped: ${it.message}")
                    AppResult.Failure(IdentityError.Error)
                }
            )

    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>
    ) = capture(event.key, params)

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>
    ) = capture(name, params)

    override fun screen(
        name: String,
        params: Map<String, Any?>
    ) {
        runCatching { gateway.screen(name, params.withoutNulls()) }
            .onFailure { PrintLogger.w(TAG, "screen($name) skipped: ${it.message}") }
    }

    override fun setUserProperty(
        key: String,
        value: String?
    ) {
        if (value == null) return
        runCatching { gateway.setPersonProperties(mapOf(key to value)) }
            .onFailure { PrintLogger.w(TAG, "setUserProperty($key) skipped: ${it.message}") }
    }

    private fun capture(
        name: String,
        params: Map<String, Any?>
    ) {
        runCatching { gateway.capture(name, params.withoutNulls()) }
            .onFailure { PrintLogger.w(TAG, "capture($name) skipped: ${it.message}") }
    }

    private fun Map<String, Any?>.withoutNulls(): Map<String, Any> =
        buildMap {
            this@withoutNulls.forEach { (key, value) -> if (value != null) put(key, value) }
        }

    private companion object {
        const val TAG = "PostHogAnalytics"
    }
}