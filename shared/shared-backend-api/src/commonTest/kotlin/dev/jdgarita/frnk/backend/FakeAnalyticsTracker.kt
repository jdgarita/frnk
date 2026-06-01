package dev.jdgarita.frnk.backend

/**
 * Recording test double for [AnalyticsTracker] — same role as [FakeAuthService]. Captures every
 * tracked event + user property so a test can assert what was emitted, without a real SDK. Reused
 * by downstream analytics work (BACKLOG P1-5 / P3). Lives in `commonTest`, never ships.
 */
class FakeAnalyticsTracker : AnalyticsTracker {
    data class Tracked(
        val name: String,
        val params: Map<String, Any?>,
    )

    val tracked = mutableListOf<Tracked>()
    val userProperties = mutableMapOf<String, String?>()

    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>,
    ) {
        tracked += Tracked(event.key, params)
    }

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>,
    ) {
        tracked += Tracked(name, params)
    }

    override fun setUserProperty(
        key: String,
        value: String?,
    ) {
        userProperties[key] = value
    }
}
