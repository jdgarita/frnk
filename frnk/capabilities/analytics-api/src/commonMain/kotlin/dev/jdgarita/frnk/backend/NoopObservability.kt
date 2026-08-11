package dev.jdgarita.frnk.backend

import dev.jdgarita.frnk.identity.IdentityError
import dev.jdgarita.frnk.utils.AppResult

/**
 * No-op observability defaults. These are the safe fallback when an app opts out of analytics /
 * crash reporting (`noopObservabilityModule`) — every call is a silent `Unit`.
 *
 * They live here (SDK-free `*-api`) rather than in a backend impl because observability is a
 * **backend-independent axis** (BACKLOG P1-5): a local-storage-only app with no backend still
 * needs a binding for these interfaces.
 * Hosts wanting real analytics install `firebaseObservabilityModule` (or their own binding).
 */
class NoopAnalyticsTracker : AnalyticsTracker {
    override fun track(
        event: ToolkitEvent,
        params: Map<String, Any?>
    ) = Unit

    override fun trackCustom(
        name: String,
        params: Map<String, Any?>
    ) = Unit

    override fun setUserProperty(
        key: String,
        value: String?
    ) = Unit

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> = AppResult.Success(Unit)
}

class NoopCrashReporter : CrashReporter {
    override fun recordException(
        throwable: Throwable,
        extras: Map<String, String>
    ) = Unit

    override fun log(message: String) = Unit

    override suspend fun identify(id: String): AppResult<Unit, IdentityError> = AppResult.Success(Unit)
}