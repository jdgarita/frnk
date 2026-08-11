package dev.jdgarita.frnk.backend

import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.utils.AppResult

/**
 * Recording test double for [AnalyticsTracker] — the canonical fake pattern for the toolkit. Captures
 * every tracked event + user property so a test can assert what was emitted, without a real SDK. Reused
 * by downstream analytics work (BACKLOG P1-5 / P3). Lives in `commonTest`, never ships.
 */
class FakeAnalyticsTracker : AnalyticsTracker {
    data class Tracked(
        val name: String,
        val params: Map<String, Any?>
    )

    val tracked = mutableListOf<Tracked>()
    val userProperties = mutableMapOf<String, String?>()
    var identity: String? = null

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> {
        identity = id
        return AppResult.Success(Unit)
    }

    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>
    ) {
        tracked += Tracked(event.key, params)
    }

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>
    ) {
        tracked += Tracked(name, params)
    }

    override fun setUserProperty(
        key: String,
        value: String?
    ) {
        userProperties[key] = value
    }
}